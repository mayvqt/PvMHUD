package com.pvmhud.tracking;

public final class TimeConstants {
    public static final long GAME_TICK_MILLIS = 600L;
    public static final long CACHE_SYNC_INTERVAL_MS = 200L;

    public static final long MS_PER_SECOND = 1_000L;
    public static final long NS_PER_MS = 1_000_000L;
    public static final long NS_PER_SECOND = 1_000_000_000L;

    private TimeConstants() {
    }

    public static long secondsToNanos(double seconds) {
        return (long) (seconds * NS_PER_SECOND);
    }

    public static long secondsToNanos(int seconds) {
        return secondsToNanos((double) seconds);
    }
}
