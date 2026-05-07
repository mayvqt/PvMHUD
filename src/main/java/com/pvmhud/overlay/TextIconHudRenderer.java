package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

@Singleton
final class TextIconHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame, boolean gameIconsMode) {
        List<Segment> spells = frame.spells();
        List<Segment> stats = frame.stats();
        List<Segment> hearts = frame.hearts();
        int paddingX = HudConstants.PADDING_X;
        int paddingY = HudConstants.PADDING_Y;
        int localRowGap = rowGap();

        if (config.verticalLayout()) {
            int maxWidth = 0;
            int maxHeight = metrics.getHeight();
            int total = 0;

            for (Segment segment : stats) {
                maxWidth = Math.max(maxWidth, segmentWidth(metrics, segment, gameIconsMode));
                maxHeight = Math.max(maxHeight, iconSize(segment));
                total++;
            }
            for (Segment segment : spells) {
                maxWidth = Math.max(maxWidth, segmentWidth(metrics, segment, gameIconsMode));
                maxHeight = Math.max(maxHeight, iconSize(segment));
                total++;
            }
            for (Segment segment : hearts) {
                maxWidth = Math.max(maxWidth, segmentWidth(metrics, segment, gameIconsMode));
                maxHeight = Math.max(maxHeight, iconSize(segment));
                total++;
            }

            int width = maxWidth + paddingX * 2;
            int height = total * maxHeight + Math.max(0, total - 1) * localRowGap + paddingY * 2;

            text.drawBackground(graphics, width, height);

            int y = paddingY;
            y = drawVerticalGroup(graphics, metrics, stats, paddingX, y, maxHeight, gameIconsMode, localRowGap);
            y = drawVerticalGroup(graphics, metrics, spells, paddingX, y, maxHeight, gameIconsMode, localRowGap);
            drawVerticalGroup(graphics, metrics, hearts, paddingX, y, maxHeight, gameIconsMode, localRowGap);

            return new Dimension(width, height);
        }

        int rowCount = (!spells.isEmpty() ? 1 : 0) + (!stats.isEmpty() ? 1 : 0) + (!hearts.isEmpty() ? 1 : 0);
        int[] rowWidths = new int[rowCount];
        int[] rowHeights = new int[rowCount];
        int width = 0;
        int height = paddingY * 2;
        int idx = 0;
        idx = measureRow(metrics, spells, gameIconsMode, rowWidths, rowHeights, idx);
        idx = measureRow(metrics, stats, gameIconsMode, rowWidths, rowHeights, idx);
        measureRow(metrics, hearts, gameIconsMode, rowWidths, rowHeights, idx);

        for (int i = 0; i < rowCount; i++) {
            width = Math.max(width, rowWidths[i]);
            height += rowHeights[i];
        }

        height += Math.max(0, rowCount - 1) * localRowGap;
        width += paddingX * 2;

        text.drawBackground(graphics, width, height);

        int y = paddingY;
        int rowIndex = 0;
        rowIndex = drawRow(graphics, metrics, spells, gameIconsMode, width, rowWidths, rowHeights, rowIndex, y);
        y += rowIndex > 0 ? rowHeights[rowIndex - 1] + localRowGap : 0;
        rowIndex = drawRow(graphics, metrics, stats, gameIconsMode, width, rowWidths, rowHeights, rowIndex, y);
        y += rowIndex > 0 ? rowHeights[rowIndex - 1] + localRowGap : 0;
        drawRow(graphics, metrics, hearts, gameIconsMode, width, rowWidths, rowHeights, rowIndex, y);

        return new Dimension(width, height);
    }

    private void drawSegment(
            Graphics2D graphics,
            FontMetrics metrics,
            Segment segment,
            int x,
            int y,
            int rowHeight,
            boolean gameIconsMode
    ) {
        if (!gameIconsMode) {
            text.drawText(
                    graphics,
                    segment.text,
                    x,
                    y + text.baseline(metrics, rowHeight),
                    segment.color
            );
            return;
        }

        if (segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART) {
            drawIconOnly(graphics, segment, x, y, rowHeight);
            return;
        }

        if (segment.kind == SegmentKind.STAT) {
            drawIconWithValue(graphics, metrics, segment, x, y, rowHeight);
            return;
        }

        text.drawText(
                graphics,
                segment.text,
                x,
                y + text.baseline(metrics, rowHeight),
                segment.color
        );
    }

    private void drawIconWithValue(
            Graphics2D graphics,
            FontMetrics metrics,
            Segment segment,
            int x,
            int y,
            int rowHeight
    ) {
        int size = iconSize(segment);
        BufferedImage icon = icons.load(segment.icon, size);

        int textX = x;

        if (icon != null) {
            graphics.drawImage(icon, x, y + (rowHeight - size) / 2, null);
            textX += size + iconTextGap();
        }

        text.drawText(
                graphics,
                segment.iconText,
                textX,
                y + text.baseline(metrics, rowHeight),
                segment.color
        );
    }

    @Override
    protected int segmentWidth(FontMetrics metrics, Segment segment, boolean gameIconsMode) {
        if (!gameIconsMode) {
            return metrics.stringWidth(segment.text);
        }

        if ((segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART) && segment.icon != null) {
            return iconSize(segment);
        }

        if (segment.kind == SegmentKind.STAT && segment.icon != null) {
            return iconSize(segment) + iconTextGap() + metrics.stringWidth(segment.iconText);
        }

        return metrics.stringWidth(segment.text);
    }

    private int drawVerticalGroup(
            Graphics2D graphics,
            FontMetrics metrics,
            List<Segment> segments,
            int x,
            int y,
            int rowHeight,
            boolean gameIconsMode,
            int gap
    ) {
        for (Segment segment : segments) {
            drawSegment(graphics, metrics, segment, x, y, rowHeight, gameIconsMode);
            y += rowHeight + gap;
        }
        return y;
    }

    private int measureRow(
            FontMetrics metrics,
            List<Segment> row,
            boolean gameIconsMode,
            int[] rowWidths,
            int[] rowHeights,
            int index
    ) {
        if (row.isEmpty()) {
            return index;
        }
        rowWidths[index] = rowWidth(metrics, row, gameIconsMode);
        rowHeights[index] = rowHeight(metrics, row);
        return index + 1;
    }

    private int drawRow(
            Graphics2D graphics,
            FontMetrics metrics,
            List<Segment> row,
            boolean gameIconsMode,
            int totalWidth,
            int[] rowWidths,
            int[] rowHeights,
            int rowIndex,
            int y
    ) {
        if (row.isEmpty()) {
            return rowIndex;
        }

        int x = centeredStartX(totalWidth, rowWidths[rowIndex]);
        for (Segment segment : row) {
            int segmentWidth = segmentWidth(metrics, segment, gameIconsMode);
            drawSegment(graphics, metrics, segment, x, y, rowHeights[rowIndex], gameIconsMode);
            x += segmentWidth + groupGap();
        }
        return rowIndex + 1;
    }

}
