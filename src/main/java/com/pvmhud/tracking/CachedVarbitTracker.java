package com.pvmhud.tracking;

import net.runelite.api.Client;

import javax.inject.Inject;

public abstract class CachedVarbitTracker implements SpellStateTracker {
    @Inject
    protected Client client;

    protected long lastSyncMs;

    protected void syncIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastSyncMs >= TimeConstants.CACHE_SYNC_INTERVAL_MS) {
            lastSyncMs = now;
            performSync();
        }
    }

    protected void invalidateCache() {
        lastSyncMs = 0L;
    }

    protected abstract void performSync();
}
