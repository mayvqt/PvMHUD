package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

abstract class AbstractPvMHUDFrameOverlay extends Overlay {
    @Inject
    private Client client;

    @Inject
    protected PvMHUDConfig config;

    @Inject
    private HudFontResolver fontResolver;

    @Inject
    private HudStyleRenderer styleRenderer;

    AbstractPvMHUDFrameOverlay(OverlayPosition defaultPosition) {
        setPosition(defaultPosition);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setDragTargetable(true);
    }

    @Override
    public final Dimension render(Graphics2D graphics) {
        if (client.getLocalPlayer() == null) {
            return null;
        }

        HudFrame frame = buildFrame(System.nanoTime());
        if (frame.isEmpty()) {
            return null;
        }

        Font oldFont = graphics.getFont();
        graphics.setFont(fontResolver.resolve(oldFont));

        FontMetrics metrics = graphics.getFontMetrics();
        Dimension dimension = styleRenderer.render(graphics, metrics, frame, hudStyle());

        graphics.setFont(oldFont);
        return dimension;
    }

    protected abstract HudFrame buildFrame(long now);

    protected abstract HudStyle hudStyle();
}
