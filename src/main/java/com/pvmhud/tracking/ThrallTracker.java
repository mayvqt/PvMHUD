package com.pvmhud.tracking;

import net.runelite.api.Skill;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;

@Singleton
public class ThrallTracker extends BaseTimedSpellTracker {
    private static final int THRALL_SPAWN_DELAY_TICKS = 4;

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() != GameStateIds.RESURRECT_THRALL_COOLDOWN) {
            return;
        }

        int cooldownTicks = event.getValue();
        setCooldownActive(cooldownTicks > 0);

        if (cooldownTicks == 1) {
            startThrallTimer();
        }
    }

    @Override
    protected void sync() {
        int cooldownTicks = client.getVarbitValue(GameStateIds.RESURRECT_THRALL_COOLDOWN);
        setCooldownActive(cooldownTicks > 0);
    }

    private void startThrallTimer() {
        int durationTicks = client.getBoostedSkillLevel(Skill.MAGIC);

        if (hasMasterCombatAchievements()) {
            durationTicks *= 2;
        }

        durationTicks += THRALL_SPAWN_DELAY_TICKS;

        markActive(TimeConstants.ticksToNanos(durationTicks));
    }

    private boolean hasMasterCombatAchievements() {
        return client.getVarbitValue(VarbitID.CA_TIER_STATUS_MASTER) == 2;
    }
}