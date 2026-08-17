package com.pvmhud.overlay;

import java.awt.Color;

final class Segment {
    final SegmentKind kind;
    final String text;
    final String iconText;
    final Color color;
    final IconRef icon;
    final double progress;
    final String chipText;

    Segment(SegmentKind kind, String text, String iconText, Color color, IconRef icon) {
        this(kind, text, iconText, color, icon, -1d);
    }

    Segment(SegmentKind kind, String text, String iconText, Color color, IconRef icon, double progress) {
        this(kind, text, iconText, color, icon, progress, "");
    }

    Segment(SegmentKind kind, String text, String iconText, Color color, IconRef icon,
            double progress, String chipText) {
        this.kind = kind;
        this.text = text == null ? "" : text;
        this.iconText = iconText == null ? "" : iconText;
        this.color = color == null ? Color.WHITE : color;
        this.icon = icon;
        this.progress = progress;
        this.chipText = chipText == null ? "" : chipText;
    }

    String label() {
        if (kind == SegmentKind.SPELL || kind == SegmentKind.HEART) {
            return iconText;
        }

        return iconText.isEmpty() ? text : iconText;
    }
}
