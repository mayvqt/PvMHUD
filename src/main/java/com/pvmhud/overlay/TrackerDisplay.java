package com.pvmhud.overlay;

import com.pvmhud.tracking.SpellStateTracker;

final class TrackerDisplay {
    final SpellStateTracker tracker;
    final String text;
    final IconRef icon;
    final HudToggle enabled;

    TrackerDisplay(SpellStateTracker tracker, String text, IconRef icon, HudToggle enabled) {
        this.tracker = tracker;
        this.text = text;
        this.icon = icon;
        this.enabled = enabled;
    }
}
