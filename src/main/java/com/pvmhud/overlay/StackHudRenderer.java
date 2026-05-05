package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

@Singleton
final class StackHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        List<Segment> all = frame.allSegments();
        int iconSize = Math.max(config.statIconSize(), config.spellIconSize());
        int rowHeight = Math.max(iconSize, metrics.getHeight());
        int gap = rowGap();

        int width = 0;
        for (Segment segment : all) {
            width = Math.max(width, iconSize + iconTextGap() + 6 + metrics.stringWidth(segment.label()));
        }
        width += HudConstants.PADDING_X * 2;

        int height = all.size() * rowHeight + Math.max(0, all.size() - 1) * gap + HudConstants.PADDING_Y * 2;
        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        for (Segment segment : all) {
            drawStackItem(graphics, metrics, segment, y, iconSize, rowHeight);
            y += rowHeight + gap;
        }

        return new Dimension(width, height);
    }

    private void drawStackItem(Graphics2D graphics, FontMetrics metrics, Segment segment, int y, int iconSize, int rowHeight) {
        BufferedImage icon = icons.load(segment.icon, iconSize);
        int iconY = y + (rowHeight - iconSize) / 2;
        if (icon != null) {
            graphics.drawImage(icon, HudConstants.PADDING_X, iconY, null);
        } else {
            String fallback = segment.label();
            if (!fallback.isEmpty()) {
                text.drawText(graphics, fallback.substring(0, 1), HudConstants.PADDING_X, y + text.baseline(metrics, rowHeight), segment.color);
            }
        }

        String label = segment.label();
        if (label.isEmpty()) {
            return;
        }

        int textX = HudConstants.PADDING_X + iconSize + iconTextGap() + 4;
        graphics.setColor(segment.color);
        graphics.fillRect(HudConstants.PADDING_X + iconSize + Math.max(1, iconTextGap() / 2), y + 2, 2, rowHeight - 4);
        text.drawText(graphics, label, textX, y + text.baseline(metrics, rowHeight), segment.color);
    }
}
