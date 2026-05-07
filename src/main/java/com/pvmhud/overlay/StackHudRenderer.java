package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Singleton
final class StackHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        int iconSize = Math.max(config.statIconSize(), config.spellIconSize());
        int rowHeight = Math.max(iconSize, metrics.getHeight());
        int gap = rowGap();
        int total = frame.stats().size() + frame.spells().size() + frame.hearts().size();

        int width = 0;
        for (Segment segment : frame.stats()) {
            width = Math.max(width, iconSize + iconTextGap() + 6 + metrics.stringWidth(segment.label()));
        }
        for (Segment segment : frame.spells()) {
            width = Math.max(width, iconSize + iconTextGap() + 6 + metrics.stringWidth(segment.label()));
        }
        for (Segment segment : frame.hearts()) {
            width = Math.max(width, iconSize + iconTextGap() + 6 + metrics.stringWidth(segment.label()));
        }
        width += HudConstants.PADDING_X * 2;

        int height = total * rowHeight + Math.max(0, total - 1) * gap + HudConstants.PADDING_Y * 2;
        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        for (Segment segment : frame.stats()) {
            drawStackItem(graphics, metrics, segment, y, iconSize, rowHeight);
            y += rowHeight + gap;
        }
        for (Segment segment : frame.spells()) {
            drawStackItem(graphics, metrics, segment, y, iconSize, rowHeight);
            y += rowHeight + gap;
        }
        for (Segment segment : frame.hearts()) {
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
        if (segment.kind != SegmentKind.STAT) {
            graphics.setColor(segment.color);
            graphics.fillRect(HudConstants.PADDING_X + iconSize + Math.max(1, iconTextGap() / 2), y + 2, 2, rowHeight - 4);
        }
        text.drawText(graphics, label, textX, y + text.baseline(metrics, rowHeight), segment.color);
    }
}
