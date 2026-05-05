package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

@Singleton
final class ChipHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        List<Segment> spells = frame.spellsAndHearts();
        int gap = Math.max(1, groupGap() / 2);
        int chipHeight = chipHeight(metrics);

        if (config.verticalLayout()) {
            List<Segment> all = frame.allSegments();
            int width = maxChipWidth(metrics, all) + HudConstants.PADDING_X * 2;
            int height = all.size() * chipHeight + Math.max(0, all.size() - 1) * gap + HudConstants.PADDING_Y * 2;

            text.drawBackground(graphics, width, height);

            int y = HudConstants.PADDING_Y;
            for (Segment segment : all) {
                drawChip(graphics, metrics, segment, HudConstants.PADDING_X, y, width - HudConstants.PADDING_X * 2, chipHeight);
                y += chipHeight + gap;
            }
            return new Dimension(width, height);
        }

        int statWidth = chipsWidth(metrics, frame.stats(), gap);
        int spellWidth = chipsWidth(metrics, spells, gap);
        int width = Math.max(statWidth, spellWidth) + HudConstants.PADDING_X * 2;
        int rows = (frame.stats().isEmpty() ? 0 : 1) + (spells.isEmpty() ? 0 : 1);
        int height = rows * chipHeight + Math.max(0, rows - 1) * rowGap() + HudConstants.PADDING_Y * 2;

        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        if (!frame.stats().isEmpty()) {
            drawChipRow(graphics, metrics, frame.stats(), y, width, statWidth, gap, chipHeight);
            y += chipHeight + rowGap();
        }
        if (!spells.isEmpty()) {
            drawChipRow(graphics, metrics, spells, y, width, spellWidth, gap, chipHeight);
        }

        return new Dimension(width, height);
    }

    private void drawChipRow(Graphics2D graphics, FontMetrics metrics, List<Segment> segments, int y, int width, int rowWidth, int gap, int chipHeight) {
        int x = HudConstants.PADDING_X + Math.max(0, (width - HudConstants.PADDING_X * 2 - rowWidth) / 2);
        for (Segment segment : segments) {
            int chipWidth = chipWidth(metrics, segment);
            drawChip(graphics, metrics, segment, x, y, chipWidth, chipHeight);
            x += chipWidth + gap;
        }
    }

    private void drawChip(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int width, int height) {
        int alpha = config.backgroundAlpha();

        if (alpha > 0) {
            graphics.setColor(text.withAlpha(config.backgroundColor(), alpha));
            graphics.fillRoundRect(x, y, width, height, 7, 7);
        }

        graphics.setColor(text.withAlpha(segment.color, Math.max(90, alpha)));
        graphics.drawRoundRect(x, y, width - 1, height - 1, 7, 7);

        if (segment.kind == SegmentKind.STAT) {
            graphics.fillRoundRect(x + 1, y + 1, 3, height - 2, 4, 4);
        }

        int size = iconSize(segment);
        int iconX = segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART
                ? x + (width - size) / 2
                : x + 7;
        int iconY = y + (height - size) / 2;

        BufferedImage icon = icons.load(segment.icon, size);
        if (icon != null) {
            graphics.drawImage(icon, iconX, iconY, null);
        }

        String label = segment.label();
        if (!label.isEmpty()) {
            text.drawText(graphics, label, iconX + size + iconTextGap(), y + text.baseline(metrics, height), segment.color);
        }
    }

    private int chipWidth(FontMetrics metrics, Segment segment) {
        if (segment.kind == SegmentKind.STAT) {
            return config.statChipWidth();
        }

        if (segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART) {
            return iconSize(segment) + 10;
        }

        return iconSize(segment) + iconTextGap() + metrics.stringWidth(segment.label()) + 14;
    }

    private int chipsWidth(FontMetrics metrics, List<Segment> segments, int gap) {
        if (segments.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (Segment segment : segments) {
            width += chipWidth(metrics, segment) + gap;
        }
        return width - gap;
    }

    private int maxChipWidth(FontMetrics metrics, List<Segment> segments) {
        int width = 0;
        for (Segment segment : segments) {
            width = Math.max(width, chipWidth(metrics, segment));
        }
        return width;
    }

    private int chipHeight(FontMetrics metrics) {
        return Math.max(metrics.getHeight(), Math.max(config.statIconSize(), config.spellIconSize())) + 6;
    }
}
