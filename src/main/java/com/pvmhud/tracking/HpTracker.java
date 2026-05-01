package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HpTracker {
    @Inject
    private Client client;

    private int cachedHp;
    private long lastSyncMs;

    public int getCurrentHp() {
        syncIfNeeded();
        return cachedHp;
    }

    private void syncIfNeeded() {
        long now = System.currentTimeMillis();

        if (now - lastSyncMs >= TimeConstants.CACHE_SYNC_INTERVAL_MS) {
            lastSyncMs = now;
            cachedHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        }
    }
}
