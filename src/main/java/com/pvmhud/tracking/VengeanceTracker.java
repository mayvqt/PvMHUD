package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

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

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == VarbitID.VENGEANCE_REBOUND) {
            rebound = event.getValue();
        } else if (event.getVarbitId() == VarbitID.VENGEANCE_TIMELIMIT) {
            cooldownTicks = Math.max(0, event.getValue());
        }
    }

    @Override
    public void reset() {
        rebound = 0;
        cooldownTicks = 0;
        invalidateCache();
    }
}
