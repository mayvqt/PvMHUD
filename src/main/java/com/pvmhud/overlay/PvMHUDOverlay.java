package com.pvmhud.overlay;

import com.pvmhud.HudStyle;
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
    private HudFontResolver fontResolver;

    @Inject
    private HudSegmentBuilder segmentBuilder;

    @Inject
    private HudVisualStateManager visualStateManager;

    @Inject
    private TextIconHudRenderer textIconRenderer;

    @Inject
    private BarHudRenderer barRenderer;

    @Inject
    private ChipHudRenderer chipRenderer;

    @Inject
    private OrbHudRenderer orbRenderer;

    @Inject
    private StackHudRenderer stackRenderer;

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

        long now = System.nanoTime();
        HudFrame frame = segmentBuilder.build(now);
        if (frame.isEmpty()) {
            return null;
        }

        Font oldFont = graphics.getFont();
        graphics.setFont(fontResolver.resolve(oldFont));
        FontMetrics metrics = graphics.getFontMetrics();

        Dimension dimension = renderSelectedStyle(graphics, metrics, frame);

        graphics.setFont(oldFont);
        return dimension;
    }

    private Dimension renderSelectedStyle(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        HudStyle style = config.hudStyle();

        switch (style) {
            case GAME_ICONS:
                return textIconRenderer.render(graphics, metrics, frame, true);
            case BARS:
                return barRenderer.render(graphics, metrics, frame);
            case CHIPS:
                return chipRenderer.render(graphics, metrics, frame);
            case ORBS:
                return orbRenderer.render(graphics, metrics, frame);
            case STACK:
                return stackRenderer.render(graphics, metrics, frame);
            case TEXT:
            default:
                return textIconRenderer.render(graphics, metrics, frame, false);
        }
    }
}
