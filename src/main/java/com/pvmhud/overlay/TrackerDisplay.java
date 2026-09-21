package com.pvmhud.overlay;

import com.pvmhud.tracking.SpellStateTracker;

import java.util.function.BooleanSupplier;

final class TrackerDisplay {
    final SpellStateTracker tracker;
    final String text;
    final IconRef icon;
    final BooleanSupplier enabled;
    final VisualState state = new VisualState();

    TrackerDisplay(SpellStateTracker tracker, String text, IconRef icon, BooleanSupplier enabled) {
        this.tracker = tracker;
        this.text = text;
        this.icon = icon;
        this.enabled = enabled;
    }
}
