package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThrallTrackerTest {
    @Test
    public void activeVarbitRestoresStateWithoutInventingRemainingTime() {
        ThrallTracker tracker = new ThrallTracker();

        tracker.onVarbitChanged(activeStateChanged(1));

        assertTrue(tracker.isActive());
        assertEquals("", tracker.getDisplayText());
        assertEquals(-1d, tracker.getProgress(), 0d);

        tracker.onVarbitChanged(activeStateChanged(0));
        assertFalse(tracker.hasActiveEffect());
    }

    private static VarbitChanged activeStateChanged(int value) {
        VarbitChanged event = new VarbitChanged();
        event.setVarbitId(VarbitID.ARCEUUS_RESURRECTION_ACTIVE);
        event.setValue(value);
        return event;
    }
}
