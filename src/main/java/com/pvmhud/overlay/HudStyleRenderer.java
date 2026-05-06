package com.pvmhud.overlay;

import com.pvmhud.overlay.HudStyle;
import com.pvmhud.PvMHUDConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

@Singleton
class HudStyleRenderer {
    @Inject
    private PvMHUDConfig config;

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

    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
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
