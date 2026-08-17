package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;

@Singleton
public class DeathChargeTracker extends BaseTimedSpellTracker {
    private static final long DEATH_CHARGE_DURATION_NANOS = TimeConstants.secondsToNanos(60);
    private int activeState;

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN) {
            setCooldownActive(event.getValue() > 0);
        } else if (event.getVarbitId() == VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE) {
            int previousState = activeState;
            activeState = event.getValue();

            if (activeState > previousState) {
                markActive(DEATH_CHARGE_DURATION_NANOS);
            }
        }
    }

    @Override
    public boolean isActive() {
        if (activeState == 0 && getRemainingNanos() == 0L) {
            syncIfNeeded();
        }
        return activeState > 0 && super.isActive();
    }

    @Override
    public String getBadgeText() {
        return getRemainingNanos() > 0L && activeState > 0 ? Integer.toString(activeState) : "";
    }

    @Override
    protected void sync() {
        int cooldown = client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN);
        setCooldownActive(cooldown > 0);
        int syncedActiveState = client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE);
        if (syncedActiveState > 0 && activeState == 0 && getRemainingNanos() == 0L) {
            markActive(DEATH_CHARGE_DURATION_NANOS);
        }
        activeState = syncedActiveState;
    }

    @Override
    public void reset() {
        activeState = 0;
        super.reset();
    }
}
