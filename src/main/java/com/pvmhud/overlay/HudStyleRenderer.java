package com.pvmhud.overlay;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

@Singleton
class HudStyleRenderer {
    @Inject
    private TextIconHudRenderer textIconRenderer;

    @Inject
    private BarHudRenderer barRenderer;

    @Inject
    private ChipHudRenderer chipRenderer;

    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame, HudStyle style) {
        switch (style) {
            case GAME_ICONS:
                return textIconRenderer.render(graphics, metrics, frame, true);
            case BARS:
                return barRenderer.render(graphics, metrics, frame);
            case CHIPS:
                return chipRenderer.render(graphics, metrics, frame);
            case TEXT:
                return textIconRenderer.render(graphics, metrics, frame, false);
            default:
                throw new IllegalArgumentException("Unsupported HUD style: " + style);
        }
    }
}
