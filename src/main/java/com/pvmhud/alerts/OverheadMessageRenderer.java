package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.TimeConstants;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

@Singleton
public final class OverheadMessageRenderer extends Overlay {
    private static final int HEIGHT_OFFSET = 40;

    private final Client client;
    private final PvMHUDConfig config;
    private volatile Message message;

    @Inject
    private OverheadMessageRenderer(Client client, PvMHUDConfig config) {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    void showLocalMessage(String message, Color color) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if (client.getLocalPlayer() == null) {
            return;
        }

        long expiresAt = System.nanoTime() + TimeConstants.secondsToNanos(config.overheadAlertSeconds());
        this.message = new Message(trimmed, color == null ? Color.WHITE : color, expiresAt);
    }

    public void clear() {
        message = null;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Message current = message;
        if (current == null || current.hasExpired(System.nanoTime())) {
            if (current != null) {
                message = null;
            }
            return null;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return null;
        }

        Point location = localPlayer.getCanvasTextLocation(
                graphics,
                current.text,
                localPlayer.getLogicalHeight() + HEIGHT_OFFSET
        );
        if (location != null) {
            OverlayUtil.renderTextLocation(graphics, location, current.text, current.color);
        }
        return null;
    }

    private static final class Message {
        private final String text;
        private final Color color;
        private final long expiresAt;

        private Message(String text, Color color, long expiresAt) {
            this.text = text;
            this.color = color;
            this.expiresAt = expiresAt;
        }

        private boolean hasExpired(long now) {
            return now - expiresAt >= 0;
        }
    }
}
