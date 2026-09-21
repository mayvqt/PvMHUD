package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

@Singleton
public final class OverheadMessageRenderer {
    private static final int CLIENT_CYCLES_PER_SECOND = 1_000 / Constants.CLIENT_TICK_LENGTH;

    private final Client client;
    private final PvMHUDConfig config;
    private final OverheadAlertBatch pendingMessages = new OverheadAlertBatch();
    private Player messagePlayer;
    private String messageText;

    @Inject
    private OverheadMessageRenderer(Client client, PvMHUDConfig config) {
        this.client = client;
        this.config = config;
    }

    void showLocalMessage(String message, Color color) {
        if (client.getLocalPlayer() != null) {
            pendingMessages.add(message, color);
        }
    }

    public void flush() {
        if (pendingMessages.isEmpty()) {
            return;
        }

        String text = pendingMessages.drain();
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        localPlayer.setOverheadText(text);
        localPlayer.setOverheadCycle(Math.max(1, config.overheadAlertSeconds() * CLIENT_CYCLES_PER_SECOND));
        messagePlayer = localPlayer;
        // Core plugins such as Emojis can replace the text synchronously.
        messageText = localPlayer.getOverheadText();
    }

    public void clear() {
        pendingMessages.clear();
        if (messagePlayer != null && messagePlayer == client.getLocalPlayer()
                && messageText != null && messageText.equals(messagePlayer.getOverheadText())) {
            messagePlayer.setOverheadText("");
            messagePlayer.setOverheadCycle(0);
        }
        messagePlayer = null;
        messageText = null;
    }
}
