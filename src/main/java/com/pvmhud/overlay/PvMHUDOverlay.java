package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
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
    private PvMHUDConfig config;

    @Inject
    private HudSegmentBuilder segmentBuilder;

    @Inject
    private HudVisualStateManager visualStateManager;

    @Inject
    private HudIconCache iconCache;

    @Inject
    private HudFontResolver fontResolver;

    @Inject
    private HudStyleRenderer styleRenderer;

    public PvMHUDOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setDragTargetable(true);
    }

    public void reset() {
        visualStateManager.reset();
        segmentBuilder.reset();
    }

    public void updateFrame(long now) {
        segmentBuilder.update(now);
    }

    public void clearCachedResources() {
        iconCache.clear();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.getLocalPlayer() == null) {
            return null;
        }

        HudFrame frame = segmentBuilder.currentFrame();
        if (frame.isEmpty()) {
            return null;
        }

        Font oldFont = graphics.getFont();
        try {
            graphics.setFont(fontResolver.resolve(oldFont));
            FontMetrics metrics = graphics.getFontMetrics();
            return styleRenderer.render(graphics, metrics, frame, config.hudStyle());
        } finally {
            graphics.setFont(oldFont);
        }
    }
}
