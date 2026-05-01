package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WardOfArceuusTracker implements SpellStateTracker, ResettableTracker {
    @Inject
    private Client client;

    private long lastCastAtNanos;
    private int lastCooldownTicks;

    private long cachedDurationNanos;

    @Override
    public boolean isActive() {
        sync();
        long now = System.nanoTime();

        return lastCastAtNanos > 0L
                && (now - lastCastAtNanos) < cachedDurationNanos;
    }

    @Override
    public boolean isOnCooldown() {
        sync();
        return lastCooldownTicks > 0;
    }

    @Override
    public boolean isExpiringSoon(int soonWindowSeconds) {
        if (!isActive()) {
            return false;
        }

        long now = System.nanoTime();
        long remaining = cachedDurationNanos - (now - lastCastAtNanos);

        return remaining > 0L
                && remaining <= TimeConstants.secondsToNanos(soonWindowSeconds);
    }

    private void sync() {
        int cooldown = client.getVarbitValue(GameStateIds.WARD_OF_ARCEUUS_COOLDOWN);

        if (cooldown > 0 && lastCooldownTicks == 0) {
            lastCastAtNanos = System.nanoTime();
            cachedDurationNanos = estimateDurationNanos();
        }

        lastCooldownTicks = cooldown;
    }

    private long estimateDurationNanos() {
        double seconds = client.getBoostedSkillLevel(Skill.MAGIC) * 0.6d;
        return TimeConstants.secondsToNanos(seconds);
    }

    @Override
    public void reset() {
        lastCastAtNanos = 0L;
        lastCooldownTicks = 0;
        cachedDurationNanos = 0L;
    }
}
