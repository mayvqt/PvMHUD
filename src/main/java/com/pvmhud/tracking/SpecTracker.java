package com.pvmhud.tracking;

import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecTracker {
    private static final int SPEC_DIVISOR = 10;

    @Inject
    private Client client;

    private int cachedSpecPercent;
    private long lastSyncMs;

    public int getSpecPercent() {
        syncIfNeeded();
        return cachedSpecPercent;
    }

    private void syncIfNeeded() {
        long now = System.currentTimeMillis();

        if (now - lastSyncMs >= TimeConstants.CACHE_SYNC_INTERVAL_MS) {
            lastSyncMs = now;
            int raw = client.getVarpValue(GameStateIds.SPECIAL_ATTACK_PERCENT);
            cachedSpecPercent = Math.max(0, Math.min(100, raw / SPEC_DIVISOR));
        }
    }
}
