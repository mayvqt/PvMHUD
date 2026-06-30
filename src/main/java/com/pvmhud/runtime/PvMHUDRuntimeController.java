package com.pvmhud.runtime;

import com.pvmhud.alerts.OverheadAlertManager;
import com.pvmhud.alerts.OverheadAlertState;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.tracking.ResettableTracker;
import com.pvmhud.tracking.SpecTracker;
import com.pvmhud.tracking.TimedPotionTracker;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PvMHUDRuntimeController {
    private static final int RECENT_COMBAT_TICKS = 8;

    @Inject
    private EventBus eventBus;

    @Inject
    private Client client;

    @Inject
    private PvMHUDOverlay hudOverlay;

    @Inject
    private SpecTracker specTracker;

    @Inject
    private TimedPotionTracker timedPotionTracker;

    @Inject
    private TrackerRegistry trackerRegistry;

    @Inject
    private OverheadAlertState overheadAlertState;

    @Inject
    private OverheadAlertManager overheadAlertManager;

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
    }

    public void onClientTick(ClientTick event) {
        if (pendingAlertBaseline) {
            capturePendingAlertBaseline();
            return;
        }

        evaluatePendingSpecAlert();
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
            recentCombatTicks = RECENT_COMBAT_TICKS;
        }
    }

    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarpId() != VarPlayerID.SA_ENERGY) {
            if (TimedPotionTracker.isPotionTimerVarbit(event.getVarbitId())) {
                timedPotionTracker.updateTimer(event.getVarbitId(), event.getValue());
                overheadAlertManager.onTimedPotionTimersChanged();
            }
            return;
        }

        pendingSpecAlertEvaluation = true;
    }

    private void resetSessionState() {
        hudOverlay.reset();

        for (ResettableTracker tracker : resettableTrackers) {
            tracker.reset();
        }
        overheadAlertState.reset();
        pendingAlertBaseline = false;
        pendingSpecAlertEvaluation = false;
        recentCombatTicks = 0;
    }

    private void updateRecentCombatTicks() {
        if (client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() != null) {
            recentCombatTicks = RECENT_COMBAT_TICKS;
            return;
        }

        recentCombatTicks = Math.max(0, recentCombatTicks - 1);
    }

    private boolean hasRecentCombatContext() {
        return recentCombatTicks > 0
                || (client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() != null);
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

}
