package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PrayerTracker {
    @Inject
    private Client client;

    private int cachedPrayer;
    private long lastSyncMs;

    public int getCurrentPrayer() {
        syncIfNeeded();
        return cachedPrayer;
    }

    private void syncIfNeeded() {
        long now = System.currentTimeMillis();

        if (now - lastSyncMs >= TimeConstants.CACHE_SYNC_INTERVAL_MS) {
            lastSyncMs = now;
            cachedPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        }
    }
}
