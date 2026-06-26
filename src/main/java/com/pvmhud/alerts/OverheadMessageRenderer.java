package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Client;
import net.runelite.api.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

@Singleton
class OverheadMessageRenderer {
    private static final int CLIENT_CYCLES_PER_SECOND = 50;

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
        localPlayer.setOverheadCycle(Math.max(1, config.overheadAlertSeconds() * CLIENT_CYCLES_PER_SECOND));
    }

    private String toHexColor(Color color) {
        String hex = Integer.toHexString(color.getRGB() & 0xFFFFFF);
        return "000000".substring(hex.length()) + hex;
    }
}
