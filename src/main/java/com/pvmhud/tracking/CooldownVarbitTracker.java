package com.pvmhud.tracking;

abstract class CooldownVarbitTracker extends CachedVarbitTracker implements ResettableTracker {
    private int cachedCooldown;

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isOnCooldown() {
        syncIfNeeded();
        return cachedCooldown > 0;
    }

    @Override
    public boolean isReady() {
        syncIfNeeded();
        return cachedCooldown == 0;
    }

    @Override
    protected void performSync() {
        cachedCooldown = client.getVarbitValue(cooldownVarbitId());
    }

    @Override
    public void reset() {
        cachedCooldown = 0;
        invalidateCache();
    }

    protected abstract int cooldownVarbitId();
}
