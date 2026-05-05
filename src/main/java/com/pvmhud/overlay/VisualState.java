package com.pvmhud.overlay;

final class VisualState {
    boolean initialised;
    boolean active;
    boolean cooldown;
    boolean ready = true;
    boolean expiringSoon;
    long lastVisibleNanos;
    long lastTransitionNanos;

    void reset() {
        initialised = false;
        active = false;
        cooldown = false;
        ready = true;
        expiringSoon = false;
        lastVisibleNanos = 0L;
        lastTransitionNanos = 0L;
    }
}
