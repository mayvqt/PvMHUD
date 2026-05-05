package com.pvmhud.tracking;

public abstract class CooldownVarbitTracker extends CachedVarbitTracker implements SpellStateTracker {
    private int cooldownTicks;

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isOnCooldown() {
        syncIfNeeded();
        return cooldownTicks > 0;
    }

    @Override
    public boolean isReady() {
        syncIfNeeded();
        return cooldownTicks <= 0;
    }

    @Override
    protected void sync() {
        cooldownTicks = client.getVarbitValue(cooldownVarbitId());
    }

    @Override
    public void reset() {
        cooldownTicks = 0;
        invalidateCache();
    }

    protected final int getCooldownTicks() {
        syncIfNeeded();
        return cooldownTicks;
    }

    protected abstract int cooldownVarbitId();
}
