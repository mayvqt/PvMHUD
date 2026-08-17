package com.pvmhud;

import com.google.inject.Provides;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.overlay.PvMHUDPlayerOverlay;
import com.pvmhud.runtime.PvMHUDRuntimeController;
import net.runelite.api.Constants;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "PvM HUD",
        description = "Compact PvM HUD for stats, spell states, and cooldowns",
        tags = {"pvm", "combat", "overlay", "hud", "thrall", "spell", "prayer", "spec"}
)
public class PvMHUDPlugin extends Plugin {
    private static final String OLD_OVERHEAD_ALERT_CYCLES_KEY = "overheadAlertCycles";
    private static final String OVERHEAD_ALERT_SECONDS_KEY = "overheadAlertSeconds";
    private static final int CLIENT_CYCLES_PER_SECOND = 1_000 / Constants.CLIENT_TICK_LENGTH;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private PvMHUDOverlay hudOverlay;

    @Inject
    private PvMHUDPlayerOverlay playerOverlay;

    @Inject
    private PvMHUDRuntimeController runtimeController;

    @Provides
    PvMHUDConfig provideConfig(ConfigManager manager) {
        return manager.getConfig(PvMHUDConfig.class);
    }

    @Override
    protected void startUp() {
        migrateConfig();
        runtimeController.start();
        overlayManager.add(hudOverlay);
        overlayManager.add(playerOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(hudOverlay);
        overlayManager.remove(playerOverlay);
        runtimeController.stop();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        runtimeController.onGameStateChanged(event);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        runtimeController.onGameTick(event);
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        runtimeController.onClientTick(event);
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        runtimeController.onStatChanged(event);
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        runtimeController.onHitsplatApplied(event);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        runtimeController.onVarbitChanged(event);
    }

    private void migrateConfig() {
        String oldCycles = configManager.getConfiguration(PvMHUDConfig.GROUP, OLD_OVERHEAD_ALERT_CYCLES_KEY);
        if (oldCycles == null || configManager.getConfiguration(PvMHUDConfig.GROUP, OVERHEAD_ALERT_SECONDS_KEY) != null) {
            return;
        }

        try {
            int cycles = Integer.parseInt(oldCycles);
            int seconds = Math.max(1, Math.min(10, (int) Math.ceil(cycles / (double) CLIENT_CYCLES_PER_SECOND)));
            configManager.setConfiguration(PvMHUDConfig.GROUP, OVERHEAD_ALERT_SECONDS_KEY, seconds);
            configManager.unsetConfiguration(PvMHUDConfig.GROUP, OLD_OVERHEAD_ALERT_CYCLES_KEY);
        } catch (NumberFormatException ignored) {
            configManager.unsetConfiguration(PvMHUDConfig.GROUP, OLD_OVERHEAD_ALERT_CYCLES_KEY);
        }
    }

}
