package com.pvmhud.alerts;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverheadAlertManagerTest {
    @Test
    public void downwardCrossingOnlyFiresWhenThresholdIsCrossed() {
        assertTrue(OverheadAlertManager.crossedDown(46, 45, 45));
        assertFalse(OverheadAlertManager.crossedDown(45, 44, 45));
        assertFalse(OverheadAlertManager.crossedDown(46, 46, 45));
    }

    @Test
    public void upwardCrossingOnlyFiresWhenThresholdIsCrossed() {
        assertTrue(OverheadAlertManager.crossedUp(49, 50, 50));
        assertFalse(OverheadAlertManager.crossedUp(50, 60, 50));
        assertFalse(OverheadAlertManager.crossedUp(49, 49, 50));
    }

    @Test
    public void largeSpecRestoresRequireCombatContext() {
        assertTrue(OverheadAlertManager.isAlertableSpecRestore(10, false));
        assertFalse(OverheadAlertManager.isAlertableSpecRestore(50, false));
        assertTrue(OverheadAlertManager.isAlertableSpecRestore(50, true));
        assertFalse(OverheadAlertManager.isAlertableSpecRestore(0, true));
    }
}
