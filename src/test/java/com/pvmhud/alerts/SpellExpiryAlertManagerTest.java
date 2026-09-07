package com.pvmhud.alerts;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpellExpiryAlertManagerTest {
    @Test
    public void alertsOnlyOnEnabledTrueToFalseTransition() {
        AtomicBoolean enabled = new AtomicBoolean(true);
        AtomicBoolean active = new AtomicBoolean(true);
        SpellExpiryAlertManager.TrackedState state = new SpellExpiryAlertManager.TrackedState(
                enabled::get,
                active::get,
                () -> "ended"
        );

        assertFalse(state.update());
        assertFalse(state.update());

        active.set(false);
        assertTrue(state.update());
        assertFalse(state.update());

        enabled.set(false);
        assertFalse(state.update());
        active.set(true);
        enabled.set(true);
        assertFalse(state.update());
    }
}
