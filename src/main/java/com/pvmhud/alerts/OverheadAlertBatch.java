package com.pvmhud.alerts;

import net.runelite.client.util.ColorUtil;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/** Alerts collected on the client thread and emitted together at the end of the tick. */
final class OverheadAlertBatch {
    private final Map<String, Color> messages = new LinkedHashMap<>();

    void add(String text, Color color) {
        String trimmed = text == null ? "" : text.trim();
        if (!trimmed.isEmpty()) {
            messages.putIfAbsent(trimmed, color == null ? Color.WHITE : color);
        }
    }

    boolean isEmpty() {
        return messages.isEmpty();
    }

    String drain() {
        StringJoiner combined = new StringJoiner(" | ");
        messages.forEach((text, color) -> combined.add(ColorUtil.wrapWithColorTag(text, color)));
        clear();
        return combined.toString();
    }

    void clear() {
        messages.clear();
    }
}
