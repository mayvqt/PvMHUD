package com.pvmhud.tracking;

public interface SpellStateTracker {
    boolean isActive();

    boolean isOnCooldown();

    default boolean isExpiringSoon(int soonWindowSeconds) {
        return false;
    }

    default boolean isReady() {
        return !isActive() && !isOnCooldown();
    }

    default String getBadgeText() {
        return "";
    }

    default String getDisplayText() {
        return getBadgeText();
    }

    default double getProgress() {
        return -1d;
    }
}
