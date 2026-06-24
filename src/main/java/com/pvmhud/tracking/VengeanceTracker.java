package com.pvmhud.tracking;

import net.runelite.api.gameval.VarbitID;

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
        rebound = client.getVarbitValue(VarbitID.VENGEANCE_REBOUND);
        cooldownTicks = client.getVarbitValue(VarbitID.VENGEANCE_TIMELIMIT);
    }

    @Override
    public void reset() {
        rebound = 0;
        cooldownTicks = 0;
        invalidateCache();
    }
}
