package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeathChargeTrackerTest {
    @Test
    public void yamaChargeTransitionKeepsTimerAndUpdatesCount() {
        DeathChargeTracker tracker = new DeathChargeTracker();

        tracker.onVarbitChanged(activeStateChanged(2));
        assertTrue(tracker.isActive());
        assertEquals("2", tracker.getChipText());

        tracker.onVarbitChanged(activeStateChanged(1));
        assertTrue(tracker.isActive());
        assertEquals("1", tracker.getChipText());

        tracker.onVarbitChanged(activeStateChanged(0));
        assertEquals("", tracker.getChipText());
        assertTrue(tracker.getProgress() > 0d);
    }

    private static VarbitChanged activeStateChanged(int value) {
        VarbitChanged event = new VarbitChanged();
        event.setVarbitId(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE);
        event.setValue(value);
        return event;
    }
}
