package com.pvmhud.tracking;

import javax.inject.Singleton;

@Singleton
public class VengeanceTracker extends CachedVarbitTracker implements SpellStateTracker {
    private int rebound;        // active vengeance stored
    private int cooldownTicks;  // cooldown timer

    @Override
    public boolean isActive() {
        syncIfNeeded();
        return rebound == 1;
    }

    @Override
    public boolean isOnCooldown() {
        syncIfNeeded();

        // Only consider cooldown if vengeance is NOT active
        return rebound == 0 && cooldownTicks > 0;
    }

    @Override
    protected void sync() {
        rebound = client.getVarbitValue(GameStateIds.VENGEANCE_ACTIVE);
        cooldownTicks = client.getVarbitValue(GameStateIds.VENGEANCE_COOLDOWN);
    }

    @Override
    public void reset() {
        rebound = 0;
        cooldownTicks = 0;
        invalidateCache();
    }
}