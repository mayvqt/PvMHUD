package com.pvmhud.tracking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeConstantsTest {
    @Test
    public void convertsTicksAndSecondsToNanoseconds() {
        assertEquals(600_000_000L, TimeConstants.ticksToNanos(1));
        assertEquals(1_500_000_000L, TimeConstants.secondsToNanos(1.5d));
        assertEquals(0L, TimeConstants.ticksToNanos(-1));
        assertEquals(0L, TimeConstants.secondsToNanos(-1));
    }
}
