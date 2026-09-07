package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

@Singleton
final class HudTextRenderer {
    private static final int MAX_CACHED_COLORS = 128;
    private final PvMHUDConfig config;
    private final Map<Integer, Color> alphaColors = new HashMap<>();

    @Inject
    HudTextRenderer(PvMHUDConfig config) {
        this.config = config;
    }

    void drawText(Graphics2D graphics, String text, int x, int y, Color color) {
        if (text == null || text.isEmpty()) {
            return;
        }

        if (config.textOutline()) {
            graphics.setColor(config.outlineColor());
            graphics.drawString(text, x - 1, y);
            graphics.drawString(text, x + 1, y);
            graphics.drawString(text, x, y - 1);
            graphics.drawString(text, x, y + 1);
        } else if (config.textShadow()) {
            graphics.setColor(config.shadowColor());
            graphics.drawString(text, x + 1, y + 1);
        }

        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }

    void drawCenteredText(Graphics2D graphics, FontMetrics metrics, String text, int x, int y, int width, int height) {
        if (text == null || text.isEmpty()) {
            return;
        }

        int textWidth = metrics.stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + baseline(metrics, height);
        drawText(graphics, text, textX, textY, graphics.getColor());
    }

    void drawBackground(Graphics2D graphics, int width, int height) {
        int alpha = config.backgroundAlpha();
        if (alpha <= 0) {
            return;
        }

        graphics.setColor(withAlpha(config.backgroundColor(), alpha));
        graphics.fillRoundRect(0, 0, width, height, 8, 8);
    }

    int baseline(FontMetrics metrics, int height) {
        return ((height - metrics.getHeight()) / 2) + metrics.getAscent();
    }

    Color withAlpha(Color color, int alpha) {
        Color safeColor = color == null ? Color.BLACK : color;
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeColor.getAlpha() == safeAlpha) {
            return safeColor;
        }

        int argb = (safeAlpha << 24) | (safeColor.getRGB() & 0xFFFFFF);
        Color cached = alphaColors.get(argb);
        if (cached != null) {
            return cached;
        }

        if (alphaColors.size() >= MAX_CACHED_COLORS) {
            alphaColors.clear();
        }
        Color result = new Color(argb, true);
        alphaColors.put(argb, result);
        return result;
    }
}
