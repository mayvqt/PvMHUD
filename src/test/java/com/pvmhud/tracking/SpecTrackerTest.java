package com.pvmhud.tracking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpecTrackerTest {
    @Test
    public void convertsRuneLiteVarpUnitsToPercent() {
        assertEquals(0, SpecTracker.normalizeSpec(-10));
        assertEquals(0, SpecTracker.normalizeSpec(0));
        assertEquals(50, SpecTracker.normalizeSpec(500));
        assertEquals(100, SpecTracker.normalizeSpec(1_000));
        assertEquals(100, SpecTracker.normalizeSpec(1_100));
    }
}
