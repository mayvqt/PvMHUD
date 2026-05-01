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
}
