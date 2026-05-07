package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Singleton
final class ChipHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        var spells = frame.spells();
        var hearts = frame.hearts();
        int spellCount = spells.size() + hearts.size();
        int itemGap = groupGap();
        int lineGap = rowGap();
        int chipHeight = chipHeight(metrics);

        if (config.verticalLayout()) {
            int total = frame.stats().size() + spellCount;
            int width = maxChipWidth(metrics, frame.stats(), spells, hearts) + HudConstants.PADDING_X * 2;
            int height = total * chipHeight + Math.max(0, total - 1) * lineGap + HudConstants.PADDING_Y * 2;

            text.drawBackground(graphics, width, height);

            int y = HudConstants.PADDING_Y;
            for (Segment segment : frame.stats()) {
                drawChip(graphics, metrics, segment, HudConstants.PADDING_X, y, width - HudConstants.PADDING_X * 2, chipHeight);
                y += chipHeight + lineGap;
            }
            for (Segment segment : spells) {
                drawChip(graphics, metrics, segment, HudConstants.PADDING_X, y, width - HudConstants.PADDING_X * 2, chipHeight);
                y += chipHeight + lineGap;
            }
            for (Segment segment : hearts) {
                drawChip(graphics, metrics, segment, HudConstants.PADDING_X, y, width - HudConstants.PADDING_X * 2, chipHeight);
                y += chipHeight + lineGap;
            }
            return new Dimension(width, height);
        }

        int spellWidth = chipsWidth(metrics, spells, hearts, itemGap);
        int statWidth = chipsWidth(metrics, frame.stats(), itemGap);
        int width = Math.max(statWidth, spellWidth) + HudConstants.PADDING_X * 2;
        int rows = (frame.stats().isEmpty() ? 0 : 1) + (spellCount == 0 ? 0 : 1);
        int height = rows * chipHeight + Math.max(0, rows - 1) * lineGap + HudConstants.PADDING_Y * 2;

        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        if (spellCount > 0) {
            drawChipRows(graphics, metrics, spells, hearts, y, width, spellWidth, itemGap, chipHeight);
            y += chipHeight + lineGap;
        }
        if (!frame.stats().isEmpty()) {
            drawChipRow(graphics, metrics, frame.stats(), y, width, statWidth, itemGap, chipHeight);
        }

        return new Dimension(width, height);
    }

    private void drawChipRow(Graphics2D graphics, FontMetrics metrics, java.util.List<Segment> segments, int y, int width, int rowWidth, int gap, int chipHeight) {
        int x = centeredStartX(width, rowWidth);
        for (Segment segment : segments) {
            int chipWidth = chipWidth(metrics, segment);
            drawChip(graphics, metrics, segment, x, y, chipWidth, chipHeight);
            x += chipWidth + gap;
        }
    }

    private void drawChipRows(Graphics2D graphics, FontMetrics metrics, java.util.List<Segment> spells, java.util.List<Segment> hearts, int y, int width, int rowWidth, int gap, int chipHeight) {
        int x = centeredStartX(width, rowWidth);
        for (Segment segment : spells) {
            int chipWidth = chipWidth(metrics, segment);
            drawChip(graphics, metrics, segment, x, y, chipWidth, chipHeight);
            x += chipWidth + gap;
        }
        for (Segment segment : hearts) {
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
        int iconY = y + (height - size) / 2;

        String label = segment.label();
        boolean hasLabel = !label.isEmpty();
        boolean iconOnly = segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART || !hasLabel;

        int iconX;
        int textX = 0;

        if (iconOnly) {
            iconX = x + (width - size) / 2;
        } else {
            int labelWidth = metrics.stringWidth(label);
            int contentWidth = size + iconTextGap() + labelWidth;

            iconX = x + Math.max(0, (width - contentWidth) / 2);
            textX = iconX + size + iconTextGap();
        }

        BufferedImage icon = icons.load(segment.icon, size);
        if (icon != null) {
            graphics.drawImage(icon, iconX, iconY, null);
        }

        if (!iconOnly) {
            text.drawText(graphics, label, textX, y + text.baseline(metrics, height), segment.color);
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

    private int chipsWidth(FontMetrics metrics, java.util.List<Segment> spells, java.util.List<Segment> hearts, int gap) {
        int count = spells.size() + hearts.size();
        if (count == 0) {
            return 0;
        }

        int width = 0;
        for (Segment segment : spells) {
            width += chipWidth(metrics, segment) + gap;
        }
        for (Segment segment : hearts) {
            width += chipWidth(metrics, segment) + gap;
        }
        return width - gap;
    }

    private int chipsWidth(FontMetrics metrics, java.util.List<Segment> segments, int gap) {
        if (segments.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (Segment segment : segments) {
            width += chipWidth(metrics, segment) + gap;
        }
        return width - gap;
    }

    private int maxChipWidth(FontMetrics metrics, java.util.List<Segment> stats, java.util.List<Segment> spells, java.util.List<Segment> hearts) {
        int width = 0;
        for (Segment segment : stats) {
            width = Math.max(width, chipWidth(metrics, segment));
        }
        for (Segment segment : spells) {
            width = Math.max(width, chipWidth(metrics, segment));
        }
        for (Segment segment : hearts) {
            width = Math.max(width, chipWidth(metrics, segment));
        }
        return width;
    }

    private int chipHeight(FontMetrics metrics) {
        return Math.max(metrics.getHeight(), Math.max(config.statIconSize(), config.spellIconSize())) + 6;
    }
}
