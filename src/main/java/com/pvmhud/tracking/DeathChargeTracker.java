package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;

@Singleton
public class DeathChargeTracker extends BaseTimedSpellTracker {
    private static final long DEATH_CHARGE_DURATION_NANOS = TimeConstants.secondsToNanos(60);

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN) {
            setCooldownActive(event.getValue() > 0);
        } else if (event.getVarbitId() == VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE) {
            if (event.getValue() > 0) {
                markActive(DEATH_CHARGE_DURATION_NANOS);
            } else {
                clearActive();
            }
        }
    }

    @Override
    protected void sync() {
        int cooldown = client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_COOLDOWN);
        setCooldownActive(cooldown > 0);
        boolean active = client.getVarbitValue(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE) > 0;
        if (active && !isActive()) {
            markActive(DEATH_CHARGE_DURATION_NANOS);
        } else if (!active) {
            clearActive();
        }
    }
}
