package com.pvmhud.overlay;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

@Singleton
public class PvMHUDOverlay extends Overlay {
    @Inject
    private Client client;

    @Inject
    private HudFontResolver fontResolver;

    @Inject
    private HudSegmentBuilder segmentBuilder;

    @Inject
    private HudVisualStateManager visualStateManager;

    @Inject
    private HudStyleRenderer styleRenderer;

    public PvMHUDOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setDragTargetable(true);
    }

    public void reset() {
        visualStateManager.reset();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.getLocalPlayer() == null) {
            return null;
        }

        HudFrame frame = segmentBuilder.build(System.nanoTime());
        if (frame.isEmpty()) {
            return null;
        }

        Font oldFont = graphics.getFont();
        graphics.setFont(fontResolver.resolve(oldFont));

        FontMetrics metrics = graphics.getFontMetrics();
        Dimension dimension = styleRenderer.render(graphics, metrics, frame);

        graphics.setFont(oldFont);
        return dimension;
    }
}
