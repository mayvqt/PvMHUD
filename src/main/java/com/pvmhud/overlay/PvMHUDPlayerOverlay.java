package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;

@Singleton
public class PvMHUDPlayerOverlay extends Overlay {
    @Inject
    private Client client;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private HudSegmentBuilder segmentBuilder;

    @Inject
    private HudFontResolver fontResolver;

    @Inject
    private PlayerBoundHudRenderer renderer;

    public PvMHUDPlayerOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (config.hudStyle() != HudStyle.PLAYER_BOUND) {
            return null;
        }

        Player player = client.getLocalPlayer();
        if (player == null) {
            return null;
        }

        HudFrame frame = segmentBuilder.build(System.nanoTime());
        if (frame.isEmpty()) {
            return null;
        }

        LocalPoint localLocation = player.getLocalLocation();
        int logicalHeight = player.getLogicalHeight();
        int plane = client.getPlane();
        Point playerHead = Perspective.localToCanvas(client, localLocation, plane, logicalHeight);
        Point playerLowerBody = Perspective.localToCanvas(client, localLocation, plane, logicalHeight / 4);
        if (playerHead == null || playerLowerBody == null) {
            return null;
        }

        Font oldFont = graphics.getFont();
        try {
            graphics.setFont(fontResolver.resolve(oldFont));
            renderer.render(graphics, graphics.getFontMetrics(), frame,
                    new java.awt.Point(playerHead.getX(), playerHead.getY()),
                    new java.awt.Point(playerLowerBody.getX(), playerLowerBody.getY()));
        } finally {
            graphics.setFont(oldFont);
        }
        return null;
    }
}
