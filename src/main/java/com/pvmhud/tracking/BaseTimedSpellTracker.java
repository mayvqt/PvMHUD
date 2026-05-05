package com.pvmhud.tracking;

public abstract class BaseTimedSpellTracker extends CachedVarbitTracker implements SpellStateTracker {
    private long activeStartedAtNanos;
    private long activeDurationNanos;
    private boolean cooldownActive;
    private boolean expiringSoon;

    protected final void markActive(long durationNanos) {
        activeStartedAtNanos = System.nanoTime();
        activeDurationNanos = Math.max(0L, durationNanos);
        expiringSoon = false;
    }

    protected final void clearActive() {
        activeStartedAtNanos = 0L;
        activeDurationNanos = 0L;
        expiringSoon = false;
    }

    protected final void setCooldownActive(boolean cooldownActive) {
        this.cooldownActive = cooldownActive;
    }

    protected final void setExpiringSoon(boolean expiringSoon) {
        this.expiringSoon = expiringSoon;
    }

    protected final long getActiveStartedAtNanos() {
        return activeStartedAtNanos;
    }

    protected final long getActiveDurationNanos() {
        return activeDurationNanos;
    }

    @Override
    public boolean isActive() {
        return activeStartedAtNanos > 0L
                && activeDurationNanos > 0L
                && System.nanoTime() - activeStartedAtNanos < activeDurationNanos;
    }

    @Override
    public boolean isOnCooldown() {
        syncIfNeeded();
        return cooldownActive;
    }

    @Override
    public boolean isExpiringSoon(int soonWindowSeconds) {
        if (!isActive()) {
            return false;
        }

        if (expiringSoon) {
            return true;
        }

        long elapsedNanos = System.nanoTime() - activeStartedAtNanos;
        long remainingNanos = activeDurationNanos - elapsedNanos;
        return remainingNanos > 0L
                && remainingNanos <= TimeConstants.secondsToNanos(soonWindowSeconds);
    }

    @Override
    public void reset() {
        activeStartedAtNanos = 0L;
        activeDurationNanos = 0L;
        cooldownActive = false;
        expiringSoon = false;
        invalidateCache();
    }
}
