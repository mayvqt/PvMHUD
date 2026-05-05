package com.pvmhud;

import com.google.inject.Provides;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.HpTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.PrayerTracker;
import com.pvmhud.tracking.ResettableTracker;
import com.pvmhud.tracking.SpecTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(
        name = "PvM HUD",
        description = "Compact PvM HUD for HP, prayer, special attack, and spell states",
        tags = {"pvm", "combat", "overlay", "hud", "thrall", "spell"}
)
public class PvMHUDPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private EventBus eventBus;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private PvMHUDOverlay hudOverlay;

    @Inject
    private HpTracker hpTracker;

    @Inject
    private PrayerTracker prayerTracker;

    @Inject
    private SpecTracker specTracker;

    @Inject
    private ThrallTracker thrallTracker;

    @Inject
    private MarkOfDarknessTracker markOfDarknessTracker;

    @Inject
    private WardOfArceuusTracker wardOfArceuusTracker;

    @Inject
    private DeathChargeTracker deathChargeTracker;

    @Inject
    private VengeanceTracker vengeanceTracker;

    @Inject
    private CorruptionTracker corruptionTracker;

    @Inject
    private HeartTracker heartTracker;

    private final List<Object> eventSubscribers = new ArrayList<>();
    private final List<ResettableTracker> resettableTrackers = new ArrayList<>();

    private int previousHitpoints = -1;
    private int previousPrayer = -1;
    private int previousSpec = -1;
    private boolean baselineReady;

    @Provides
    PvMHUDConfig provideConfig(ConfigManager manager) {
        return manager.getConfig(PvMHUDConfig.class);
    }

    @Override
    protected void startUp() {
        initialiseTrackerLists();
        resetSessionState();

        for (Object subscriber : eventSubscribers) {
            eventBus.register(subscriber);
        }

        overlayManager.add(hudOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(hudOverlay);

        for (Object subscriber : eventSubscribers) {
            eventBus.unregister(subscriber);
        }

        resetSessionState();
        eventSubscribers.clear();
        resettableTrackers.clear();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();

        if (state == GameState.LOGGED_IN) {
            captureBaseline();
            return;
        }

        // Do not reset on LOADING. Region loads and instances should not wipe spell timers.
        if (state == GameState.HOPPING || state == GameState.LOGIN_SCREEN) {
            resetSessionState();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (!baselineReady) {
            return;
        }

        if (event.getSkill() == Skill.HITPOINTS) {
            int currentHp = event.getBoostedLevel();
            if (config.overheadHpAlertEnabled()
                    && crossedDown(previousHitpoints, currentHp, config.hpLowThreshold())) {
                showLocalOverheadMessage(config.lowHpOverheadMessage(), config.hpLowColor());
            }
            previousHitpoints = currentHp;
        } else if (event.getSkill() == Skill.PRAYER) {
            int currentPrayer = event.getBoostedLevel();
            if (config.overheadPrayerAlertEnabled()
                    && crossedDown(previousPrayer, currentPrayer, config.prayerLowThreshold())) {
                showLocalOverheadMessage(config.lowPrayerOverheadMessage(), config.prayerLowColor());
            }
            previousPrayer = currentPrayer;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!baselineReady) {
            return;
        }

        int currentSpec = specTracker.getSpecPercent();
        if (config.overheadSpecAlertEnabled()
                && previousSpec >= 0
                && crossedUp(previousSpec, currentSpec, config.specThreshold())) {
            showLocalOverheadMessage(config.specOverheadMessage(), config.specHighColor());
        }
        previousSpec = currentSpec;
    }

    private void initialiseTrackerLists() {
        if (!eventSubscribers.isEmpty()) {
            return;
        }

        eventSubscribers.add(hpTracker);
        eventSubscribers.add(prayerTracker);
        eventSubscribers.add(specTracker);
        eventSubscribers.add(thrallTracker);
        eventSubscribers.add(markOfDarknessTracker);
        eventSubscribers.add(wardOfArceuusTracker);
        eventSubscribers.add(deathChargeTracker);
        eventSubscribers.add(vengeanceTracker);
        eventSubscribers.add(corruptionTracker);
        eventSubscribers.add(heartTracker);

        resettableTrackers.add(hpTracker);
        resettableTrackers.add(prayerTracker);
        resettableTrackers.add(specTracker);
        resettableTrackers.add(thrallTracker);
        resettableTrackers.add(markOfDarknessTracker);
        resettableTrackers.add(wardOfArceuusTracker);
        resettableTrackers.add(deathChargeTracker);
        resettableTrackers.add(vengeanceTracker);
        resettableTrackers.add(corruptionTracker);
        resettableTrackers.add(heartTracker);
    }

    private void resetSessionState() {
        hudOverlay.reset();

        for (ResettableTracker tracker : resettableTrackers) {
            tracker.reset();
        }

        previousHitpoints = -1;
        previousPrayer = -1;
        previousSpec = -1;
        baselineReady = false;
    }

    private void captureBaseline() {
        previousHitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
        previousPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        previousSpec = specTracker.getSpecPercent();
        baselineReady = true;
    }


    private void showLocalOverheadMessage(String message, Color color) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        localPlayer.setOverheadText("<col=" + toHexColor(color) + ">" + trimmed + "</col>");
        localPlayer.setOverheadCycle(config.overheadAlertCycles());
    }

    private static String toHexColor(Color color) {
        Color safeColor = color == null ? Color.WHITE : color;
        return String.format("%02x%02x%02x", safeColor.getRed(), safeColor.getGreen(), safeColor.getBlue());
    }

    private static boolean crossedDown(int previousValue, int currentValue, int threshold) {
        return previousValue > threshold && currentValue <= threshold;
    }

    private static boolean crossedUp(int previousValue, int currentValue, int threshold) {
        return previousValue < threshold && currentValue >= threshold;
    }
}
