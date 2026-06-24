package com.pvmhud.tracking;

import net.runelite.api.Client;

import javax.inject.Inject;

public abstract class CachedVarbitTracker implements ResettableTracker {
    private static final long CACHE_SYNC_INTERVAL_NANOS =
            TimeConstants.CACHE_SYNC_INTERVAL_MS * TimeConstants.NS_PER_MS;

    @Inject
    protected Client client;

    private long lastSyncNanos;

    protected final void syncIfNeeded() {
        long now = System.nanoTime();
        if (lastSyncNanos == 0L || now - lastSyncNanos >= CACHE_SYNC_INTERVAL_NANOS) {
            lastSyncNanos = now;
            sync();
        }
    }

    protected final void invalidateCache() {
        lastSyncNanos = 0L;
    }

    protected abstract void sync();
}
