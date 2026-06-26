package com.pvmhud.overlay;

import java.awt.Color;

final class Segment {
    final SegmentKind kind;
    final String text;
    final String iconText;
    final Color color;
    final IconRef icon;

    Segment(SegmentKind kind, String text, String iconText, Color color, IconRef icon) {
        this.kind = kind;
        this.text = text == null ? "" : text;
        this.iconText = iconText == null ? "" : iconText;
        this.color = color == null ? Color.WHITE : color;
        this.icon = icon;
    }

    String label() {
        if (kind == SegmentKind.SPELL || kind == SegmentKind.HEART) {
            return "";
        }

        if (kind == SegmentKind.POTION) {
            return iconText.isEmpty() ? text : iconText;
        }

        return iconText.isEmpty() ? text : iconText;
    }
}
