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
            } else if (activeState == 0 && hasUnknownActiveDuration()) {
                clearActive();
            }
        }
    }

    @Override
    public boolean isActive() {
        if (!hasTrackedActiveEffect()) {
            syncIfNeeded();
        }
        return activeState > 0 && super.isActive();
    }

    @Override
    public String getBadgeText() {
        return hasTrackedActiveEffect() && activeState > 0 ? Integer.toString(activeState) : "";
    }

    @Override
    protected void sync() {
        int cooldown = client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN);
        int syncedActiveState = client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE);
        restoreFromClientState(cooldown, syncedActiveState);
    }

    void restoreFromClientState(int cooldown, int syncedActiveState) {
        setCooldownActive(cooldown > 0);
        if (syncedActiveState > 0 && !hasTrackedActiveEffect()) {
            markActiveWithoutKnownDuration();
        } else if (syncedActiveState == 0 && hasUnknownActiveDuration()) {
            clearActive();
        }
        activeState = syncedActiveState;
    }

    @Override
    public void reset() {
        activeState = 0;
        super.reset();
    }
}
