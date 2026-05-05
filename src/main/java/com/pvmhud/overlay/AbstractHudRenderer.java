package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;

import javax.inject.Inject;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractHudRenderer {
    @Inject
    protected PvMHUDConfig config;

    @Inject
    protected HudIconCache icons;

    @Inject
    protected HudTextRenderer text;

    protected List<List<Segment>> standardRows(HudFrame frame) {
        List<List<Segment>> rows = new ArrayList<>(3);

        if (!frame.spells().isEmpty()) {
            rows.add(frame.spells());
        }
        if (!frame.stats().isEmpty()) {
            rows.add(frame.stats());
        }
        if (!frame.hearts().isEmpty()) {
            rows.add(frame.hearts());
        }

        return rows;
    }

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
}
