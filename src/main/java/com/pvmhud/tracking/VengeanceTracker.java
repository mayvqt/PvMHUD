package com.pvmhud.tracking;

import javax.inject.Singleton;

@Singleton
public class VengeanceTracker extends CachedVarbitTracker implements ResettableTracker {
    private int cachedActive;
    private int cachedCooldown;

    @Override
    public boolean isActive() {
        syncIfNeeded();
        return cachedActive == 1;
    }

    @Override
    public boolean isOnCooldown() {
        syncIfNeeded();
        return cachedCooldown > 0;
    }

    @Override
    public boolean isExpiringSoon(int soonWindowSeconds) {
        return false;
    }

    @Override
    protected void performSync() {
        cachedActive = client.getVarbitValue(GameStateIds.VENGEANCE_ACTIVE);
        cachedCooldown = client.getVarbitValue(GameStateIds.VENGEANCE_COOLDOWN);
    }

    @Override
    public void reset() {
        cachedActive = 0;
        cachedCooldown = 0;
        invalidateCache();
    }
}
