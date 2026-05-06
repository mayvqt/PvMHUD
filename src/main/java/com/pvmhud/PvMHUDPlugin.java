package com.pvmhud;

import com.google.inject.Provides;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.runtime.PvMHUDRuntimeController;
import net.runelite.api.events.GameStateChanged;
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
        description = "Compact PvM HUD for HP, prayer, special attack, and spell states",
        tags = {"pvm", "combat", "overlay", "hud", "thrall", "spell"}
)
public class PvMHUDPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PvMHUDOverlay hudOverlay;

    @Inject
    private PvMHUDRuntimeController runtimeController;

    @Provides
    PvMHUDConfig provideConfig(ConfigManager manager) {
        return manager.getConfig(PvMHUDConfig.class);
    }

    @Override
    protected void startUp() {
        runtimeController.start();
        overlayManager.add(hudOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(hudOverlay);
        runtimeController.stop();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        runtimeController.onGameStateChanged(event);
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        runtimeController.onStatChanged(event);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        runtimeController.onVarbitChanged(event);
    }
}
