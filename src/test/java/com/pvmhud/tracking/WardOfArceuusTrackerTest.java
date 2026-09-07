package com.pvmhud.tracking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WardOfArceuusTrackerTest {
    @Test
    public void durationUsesSixTenthsOfRealMagicLevel() {
        assertEquals(TimeConstants.secondsToNanos(59.4d), WardOfArceuusTracker.estimateDurationNanos(99));
        assertEquals(0L, WardOfArceuusTracker.estimateDurationNanos(-1));
    }
}
