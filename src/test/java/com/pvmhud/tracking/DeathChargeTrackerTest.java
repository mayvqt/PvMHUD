package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeathChargeTrackerTest {
    @Test
    public void yamaChargeTransitionKeepsTimerAndUpdatesCount() {
        DeathChargeTracker tracker = new DeathChargeTracker();

        tracker.onVarbitChanged(activeStateChanged(2));
        assertTrue(tracker.isActive());
        assertEquals("2", tracker.getBadgeText());

        tracker.onVarbitChanged(activeStateChanged(1));
        assertTrue(tracker.isActive());
        assertEquals("1", tracker.getBadgeText());

        tracker.onVarbitChanged(activeStateChanged(0));
        assertEquals("", tracker.getBadgeText());
        assertTrue(tracker.getProgress() > 0d);
    }

    @Test
    public void restoredActiveStateDoesNotInventFreshDuration() {
        DeathChargeTracker tracker = new DeathChargeTracker();

        tracker.restoreFromClientState(1, 1);

        assertTrue(tracker.isActive());
        assertEquals("1", tracker.getBadgeText());
        assertEquals("", tracker.getDisplayText());
        assertEquals(-1d, tracker.getProgress(), 0d);

        tracker.restoreFromClientState(1, 0);
        assertFalse(tracker.hasActiveEffect());
    }

    private static VarbitChanged activeStateChanged(int value) {
        VarbitChanged event = new VarbitChanged();
        event.setVarbitId(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE);
        event.setValue(value);
        return event;
    }
}
