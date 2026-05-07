package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;

import javax.inject.Inject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.util.List;

abstract class AbstractHudRenderer {
    @Inject
    protected PvMHUDConfig config;

    @Inject
    protected HudIconCache icons;

    @Inject
    protected HudTextRenderer text;

    protected int iconSize(Segment segment) {
        return segment.kind == SegmentKind.STAT ? config.statIconSize() : config.spellIconSize();
    }

    protected int groupGap() {
        return Math.max(0, config.groupGap());
    }

    protected int rowGap() {
        return Math.max(0, config.rowGap());
    }

    protected int barGap() {
        return Math.max(0, config.barGap());
    }

    protected int iconTextGap() {
        return Math.max(0, config.iconTextGap());
    }

    protected int rowHeight(FontMetrics metrics, List<Segment> segments) {
        int height = metrics.getHeight();
        for (Segment segment : segments) {
            height = Math.max(height, iconSize(segment));
        }
        return height;
    }

    protected int segmentWidth(FontMetrics metrics, Segment segment, boolean iconsOnly) {
        if (iconsOnly && segment.icon != null) {
            return iconSize(segment);
        }
        return metrics.stringWidth(segment.text);
    }

    protected int rowWidth(FontMetrics metrics, List<Segment> segments, boolean iconsOnly) {
        if (segments.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (Segment segment : segments) {
            width += segmentWidth(metrics, segment, iconsOnly) + groupGap();
        }
        return width - groupGap();
    }

    protected int centeredStartX(int contentWidth, int rowWidth) {
        return HudConstants.PADDING_X + Math.max(0, (contentWidth - HudConstants.PADDING_X * 2 - rowWidth) / 2);
    }

    protected void drawIconOnly(Graphics2D graphics, Segment segment, int x, int y, int rowHeight) {
        int size = iconSize(segment);
        BufferedImage icon = icons.load(segment.icon, size);

        if (icon != null) {
            graphics.drawImage(icon, x, y + (rowHeight - size) / 2, null);
        }
    }

    protected void drawSpellTile(Graphics2D graphics, Segment segment, int x, int y, int size) {
        int alpha = config.backgroundAlpha();
        graphics.setColor(text.withAlpha(segment.color, Math.max(55, alpha / 2)));
        graphics.fillRoundRect(x, y, size, size, 6, 6);
        graphics.setColor(text.withAlpha(segment.color, 220));
        graphics.drawRoundRect(x, y, size - 1, size - 1, 6, 6);

        BufferedImage icon = icons.load(segment.icon, Math.max(10, size - 6));
        if (icon != null) {
            int iconX = x + (size - icon.getWidth()) / 2;
            int iconY = y + (size - icon.getHeight()) / 2;
            graphics.drawImage(icon, iconX, iconY, null);
        }
    }
}
