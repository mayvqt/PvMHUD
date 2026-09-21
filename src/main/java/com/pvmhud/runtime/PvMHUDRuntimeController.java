package com.pvmhud.runtime;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.alerts.OverheadAlertManager;
import com.pvmhud.alerts.OverheadAlertState;
import com.pvmhud.alerts.OverheadMessageRenderer;
import com.pvmhud.alerts.SpellExpiryAlertManager;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.tracking.ResettableTracker;
import com.pvmhud.tracking.SpecTracker;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PvMHUDRuntimeController {
    @Inject
    private EventBus eventBus;

    @Inject
    private Client client;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private PvMHUDOverlay hudOverlay;

    @Inject
    private SpecTracker specTracker;

    @Inject
    private TrackerRegistry trackerRegistry;

    @Inject
    private OverheadAlertState overheadAlertState;

    @Inject
    private OverheadAlertManager overheadAlertManager;

    @Inject
    private OverheadMessageRenderer overheadMessageRenderer;

    @Inject
    private SpellExpiryAlertManager spellExpiryAlertManager;

    private List<ResettableTracker> resettableTrackers = List.of();
    private boolean pendingAlertBaseline;
    private boolean pendingSpecAlertEvaluation;
    private int recentCombatTicks;

    public void start() {
        resettableTrackers = trackerRegistry.trackers();
        resetSessionState();

        for (ResettableTracker tracker : resettableTrackers) {
            eventBus.register(tracker);
        }

        if (client.getGameState() == GameState.LOGGED_IN) {
            pendingAlertBaseline = true;
        }
    }

    public void stop() {
        for (ResettableTracker tracker : resettableTrackers) {
            eventBus.unregister(tracker);
        }

        resetSessionState();
        hudOverlay.clearCachedResources();
        resettableTrackers = List.of();
    }

    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();

        if (state == GameState.LOGGED_IN) {
            overheadAlertState.reset();
            pendingAlertBaseline = true;
            return;
        }

        if (state == GameState.HOPPING || state == GameState.LOGIN_SCREEN) {
            resetSessionState();
        }
    }

    public void onGameTick(GameTick event) {
        updateRecentCombatTicks();
        hudOverlay.setInCombat(hasRecentCombatContext());

        evaluateSpellExpiryAlerts();
    }

    public void onPostClientTick(PostClientTick event) {
        if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null) {
            hudOverlay.updateFrame(System.nanoTime());
        }

        if (pendingAlertBaseline) {
            capturePendingAlertBaseline();
        } else {
            evaluatePendingSpecAlert();
        }

        if (client.getGameState() == GameState.LOGGED_IN) {
            overheadMessageRenderer.flush();
        }
    }

    private void capturePendingAlertBaseline() {
        if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null) {
            overheadAlertState.captureBaseline(specTracker.getSpecPercent());
            pendingAlertBaseline = false;
            pendingSpecAlertEvaluation = false;
        }
    }

    public void onStatChanged(StatChanged event) {
        overheadAlertManager.onStatChanged(event);
    }

    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() == client.getLocalPlayer()) {
            recentCombatTicks = combatRetentionTicks();
            hudOverlay.setInCombat(true);
        }
    }

    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarpId() == VarPlayerID.SA_ENERGY) {
            pendingSpecAlertEvaluation = true;
        }

        if (isTrackedSpellAlertVarbit(event.getVarbitId())) {
            evaluateSpellExpiryAlerts();
        }
    }

    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.GAMEMESSAGE) {
            evaluateSpellExpiryAlerts();
        }
    }

    private void resetSessionState() {
        hudOverlay.reset();

        for (ResettableTracker tracker : resettableTrackers) {
            tracker.reset();
        }
        overheadAlertState.reset();
        overheadMessageRenderer.clear();
        spellExpiryAlertManager.reset();
        pendingAlertBaseline = false;
        pendingSpecAlertEvaluation = false;
        recentCombatTicks = 0;
    }

    private void updateRecentCombatTicks() {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer != null && localPlayer.getInteracting() != null) {
            recentCombatTicks = combatRetentionTicks();
            return;
        }

        recentCombatTicks = Math.max(0, recentCombatTicks - 1);
    }

    private boolean hasRecentCombatContext() {
        Player localPlayer = client.getLocalPlayer();
        return recentCombatTicks > 0 || (localPlayer != null && localPlayer.getInteracting() != null);
    }

    private int combatRetentionTicks() {
        int millis = config.combatHideDelaySeconds() * 1_000;
        return (millis + Constants.GAME_TICK_LENGTH - 1) / Constants.GAME_TICK_LENGTH;
    }

    private void evaluatePendingSpecAlert() {
        if (!pendingSpecAlertEvaluation) {
            return;
        }

        pendingSpecAlertEvaluation = false;
        if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null) {
            overheadAlertManager.onSpecPercentChanged(specTracker.getSpecPercent(), hasRecentCombatContext());
        }
    }

    private void evaluateSpellExpiryAlerts() {
        if (client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null) {
            spellExpiryAlertManager.update();
        }
    }

    static boolean isTrackedSpellAlertVarbit(int varbitId) {
        return varbitId == VarbitID.ARCEUUS_RESURRECTION_ACTIVE
                || varbitId == VarbitID.ARCEUUS_RESURRECTION_COOLDOWN
                || varbitId == VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE
                || varbitId == VarbitID.VENGEANCE_REBOUND
                || varbitId == VarbitID.ARCEUUS_WARD_COOLDOWN
                || varbitId == VarbitID.ARCEUUS_CORRUPTION_COOLDOWN
                || varbitId == VarbitID.IMBUED_HEART_TIMER;
    }

}
