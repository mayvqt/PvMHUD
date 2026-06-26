package com.pvmhud.tracking;

public final class TimedPotionEffect {
    private final TimedPotionType type;
    private final int remainingTicks;

    TimedPotionEffect(TimedPotionType type, int remainingTicks) {
        this.type = type;
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public TimedPotionType getType() {
        return type;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public long getRemainingNanos() {
        return TimeConstants.ticksToNanos(remainingTicks);
    }

    public boolean isActive() {
        return remainingTicks > 0;
    }

    public boolean isExpiringSoon(int seconds) {
        return isActive() && getRemainingNanos() <= TimeConstants.secondsToNanos(seconds);
    }
}
