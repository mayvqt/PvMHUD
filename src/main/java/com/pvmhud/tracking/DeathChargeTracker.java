package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;

@Singleton
public class DeathChargeTracker extends BaseTimedSpellTracker {
    private static final long DEATH_CHARGE_DURATION_NANOS = TimeConstants.secondsToNanos(60);

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() != GameStateIds.DEATH_CHARGE_COOLDOWN) {
            return;
        }

        int cooldown = event.getValue();
        setCooldownActive(cooldown > 0);

        if (cooldown == 1 && !isActive()) {
            markActive(DEATH_CHARGE_DURATION_NANOS);
        } else if (cooldown == 0 && isActive()) {
            clearActive();
        }
    }

    @Override
    protected void sync() {
        int cooldown = client.getVarbitValue(GameStateIds.DEATH_CHARGE_COOLDOWN);
        setCooldownActive(cooldown > 0);
    }
}