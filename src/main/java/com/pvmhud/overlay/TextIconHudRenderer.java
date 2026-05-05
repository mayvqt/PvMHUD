package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Singleton
final class TextIconHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame, boolean iconsOnly) {
        List<List<Segment>> rows = standardRows(frame);

        if (config.verticalLayout()) {
            List<Segment> all = frame.allSegments();
            int width = maxSegmentWidth(metrics, all, iconsOnly) + HudConstants.PADDING_X * 2;
            int rowHeight = rowHeight(metrics, all);
            int height = all.size() * rowHeight + Math.max(0, all.size() - 1) * rowGap() + HudConstants.PADDING_Y * 2;

            text.drawBackground(graphics, width, height);

            int y = HudConstants.PADDING_Y;
            for (Segment segment : all) {
                drawSegment(graphics, metrics, segment, HudConstants.PADDING_X, y, rowHeight, iconsOnly);
                y += rowHeight + rowGap();
            }
            return new Dimension(width, height);
        }

        int width = 0;
        int height = HudConstants.PADDING_Y * 2;
        for (List<Segment> row : rows) {
            width = Math.max(width, rowWidth(metrics, row, iconsOnly));
            height += rowHeight(metrics, row);
        }
        height += Math.max(0, rows.size() - 1) * rowGap();
        width += HudConstants.PADDING_X * 2;

        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        for (List<Segment> row : rows) {
            int rowHeight = rowHeight(metrics, row);
            int rowWidth = rowWidth(metrics, row, iconsOnly);
            int x = HudConstants.PADDING_X + (width - HudConstants.PADDING_X * 2 - rowWidth) / 2;
            for (Segment segment : row) {
                int segmentWidth = segmentWidth(metrics, segment, iconsOnly);
                drawSegment(graphics, metrics, segment, x, y, rowHeight, iconsOnly);
                x += segmentWidth + groupGap();
            }
            y += rowHeight + rowGap();
        }

        return new Dimension(width, height);
    }

    private void drawSegment(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int rowHeight, boolean iconsOnly) {
        if (iconsOnly && segment.icon != null) {
            int size = iconSize(segment);
            BufferedImage icon = icons.load(segment.icon, size);
            if (icon != null) {
                graphics.drawImage(icon, x, y + (rowHeight - size) / 2, null);
                return;
            }
        }

        text.drawText(graphics, segment.text, x, y + text.baseline(metrics, rowHeight), segment.color);
    }

    private int maxSegmentWidth(FontMetrics metrics, List<Segment> segments, boolean iconsOnly) {
        int width = 0;
        for (Segment segment : segments) {
            width = Math.max(width, segmentWidth(metrics, segment, iconsOnly));
        }
        return width;
    }
}
