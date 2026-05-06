package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Client;
import net.runelite.api.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

@Singleton
class OverheadMessageRenderer {
    @Inject
    private Client client;

    @Inject
    private PvMHUDConfig config;

    void showLocalMessage(String message, Color color) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        localPlayer.setOverheadText("<col=" + toHexColor(color) + ">" + trimmed);
        localPlayer.setOverheadCycle(config.overheadAlertCycles());
    }

    private String toHexColor(Color color) {
        return String.format("%06x", color.getRGB() & 0xFFFFFF);
    }
}
