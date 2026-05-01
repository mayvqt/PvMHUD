package com.pvmhud;

import com.google.inject.Provides;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.tracking.*;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.Color;

@PluginDescriptor(
        name = "PvM HUD",
        description = "Compact PvM HUD for HP, prayer, spec, and spell states",
        tags = {"pvm", "combat", "overlay", "hud"}
)
public class PvMHUDPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private Client client;
    @Inject
    private PvMHUDConfig config;
    @Inject
    private PvMHUDOverlay overlay;
    @Inject
    private EventBus eventBus;

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

    private boolean lowHpAlertActive;
    private boolean lowPrayerAlertActive;

    @Override
    protected void startUp() {
        resetState();

        eventBus.register(thrallTracker);
        eventBus.register(markOfDarknessTracker);
        eventBus.register(deathChargeTracker);

        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);

        eventBus.unregister(thrallTracker);
        eventBus.unregister(markOfDarknessTracker);
        eventBus.unregister(deathChargeTracker);

        resetState();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();

        if (state == GameState.HOPPING
                || state == GameState.LOADING
                || state == GameState.LOGGING_IN
                || state == GameState.LOGIN_SCREEN) {
            resetState();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (!config.overheadThresholdAlerts()) {
            lowHpAlertActive = false;
            lowPrayerAlertActive = false;
            return;
        }

        if (event.getSkill() == Skill.HITPOINTS) {
            lowHpAlertActive = updateThresholdAlert(
                    event.getBoostedLevel(),
                    config.hpLowThreshold(),
                    lowHpAlertActive,
                    config.lowHpOverheadMessage(),
                    config.hpLowColor()
            );
        } else if (event.getSkill() == Skill.PRAYER) {
            lowPrayerAlertActive = updateThresholdAlert(
                    event.getBoostedLevel(),
                    config.prayerLowThreshold(),
                    lowPrayerAlertActive,
                    config.lowPrayerOverheadMessage(),
                    config.prayerLowColor()
            );
        }
    }

    @Provides
    PvMHUDConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PvMHUDConfig.class);
    }

    private void resetState() {
        overlay.reset();

        lowHpAlertActive = false;
        lowPrayerAlertActive = false;

        thrallTracker.reset();
        deathChargeTracker.reset();
        markOfDarknessTracker.reset();
        wardOfArceuusTracker.reset();
        vengeanceTracker.reset();
        corruptionTracker.reset();
        heartTracker.reset();
    }

    private boolean updateThresholdAlert(int currentValue, int threshold, boolean alertActive, String message, Color color) {
        if (currentValue > threshold) {
            return false;
        }

        if (!alertActive) {
            showLocalOverheadMessage(message, color);
        }

        return true;
    }

    private void showLocalOverheadMessage(String message, Color color) {
        String trimmedMessage = message == null ? "" : message.trim();
        if (trimmedMessage.isEmpty()) {
            return;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer != null) {
            localPlayer.setOverheadText(colorTag(color) + trimmedMessage + "</col>");
            localPlayer.setOverheadCycle(config.overheadAlertCycles());
        }
    }

    private String colorTag(Color color) {
        Color safeColor = color == null ? Color.WHITE : color;
        int rgb = safeColor.getRGB() & 0xFFFFFF;
        return "<col=" + Integer.toHexString(0x1000000 | rgb).substring(1) + ">";
    }
}
