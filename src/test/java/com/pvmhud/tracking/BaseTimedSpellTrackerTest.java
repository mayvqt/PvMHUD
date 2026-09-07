package com.pvmhud.tracking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseTimedSpellTrackerTest {
    @Test
    public void unknownDurationRepresentsActiveEffectWithoutInventingTimer() {
        TestTracker tracker = new TestTracker();

        tracker.activateWithoutKnownDuration();

        assertTrue(tracker.isActive());
        assertTrue(tracker.hasActiveEffect());
        assertEquals("", tracker.getDisplayText());
        assertEquals(-1d, tracker.getProgress(), 0d);
        assertFalse(tracker.isExpiringSoon(60));

        tracker.reset();
        assertFalse(tracker.isActive());
    }

    private static final class TestTracker extends BaseTimedSpellTracker {
        void activateWithoutKnownDuration() {
            markActiveWithoutKnownDuration();
        }

        @Override
        protected void sync() {
        }
    }
}
