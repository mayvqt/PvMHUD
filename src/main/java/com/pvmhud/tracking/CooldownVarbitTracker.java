package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

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

    @Subscribe
    public final void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == cooldownVarbitId()) {
            cooldownTicks = Math.max(0, event.getValue());
        }
    }

    @Override
    public void reset() {
        cooldownTicks = 0;
        invalidateCache();
    }

    protected abstract int cooldownVarbitId();
}
