package com.pvmhud.tracking;

import net.runelite.api.Client;

import javax.inject.Inject;

public abstract class CachedVarbitTracker implements ResettableTracker {
    @Inject
    protected Client client;

    private long lastSyncMillis;

    protected final void syncIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastSyncMillis >= TimeConstants.CACHE_SYNC_INTERVAL_MS) {
            lastSyncMillis = now;
            sync();
        }
    }

    protected final void forceSync() {
        lastSyncMillis = System.currentTimeMillis();
        sync();
    }

    protected final void invalidateCache() {
        lastSyncMillis = 0L;
    }

    protected abstract void sync();
}
