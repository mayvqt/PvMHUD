package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MarkOfDarknessTrackerTest {
    private static final String PLACED = "You have placed a Mark of Darkness upon yourself.";
    private static final String EXPIRING = "Your Mark of Darkness is about to run out.";
    private static final String FADED = "Your Mark of Darkness has faded away.";
    private static final String[] FORMATS = {
            "%s",
            "<col=6800bf>%s</col>",
            "@mes_hl_pur@%s</col>"
    };

    @Test
    public void tracksActivationWarningAndExpiryAcrossMessageFormats() {
        for (String format : FORMATS) {
            MarkOfDarknessTracker tracker = new TestTracker();

            tracker.onChatMessage(gameMessage(String.format(format, PLACED)));
            assertTrue(format, tracker.isActive());
            assertTrue(format, tracker.getProgress() > 0d);
            assertFalse(format, tracker.isExpiringSoon(0));

            tracker.onChatMessage(gameMessage(String.format(format, EXPIRING)));
            assertTrue(format, tracker.isActive());
            assertTrue(format, tracker.isExpiringSoon(0));

            tracker.onChatMessage(gameMessage(String.format(format, FADED)));
            assertFalse(format, tracker.hasActiveEffect());
            assertFalse(format, tracker.isExpiringSoon(0));
            assertEquals(format, "", tracker.getDisplayText());
        }
    }

    @Test
    public void recastingClearsThePreviousWarning() {
        MarkOfDarknessTracker tracker = new TestTracker();
        tracker.onChatMessage(gameMessage("@mes_hl_pur@" + PLACED + "</col>"));
        tracker.onChatMessage(gameMessage("@mes_hl_pur@" + EXPIRING + "</col>"));
        assertTrue(tracker.isExpiringSoon(0));

        tracker.onChatMessage(gameMessage("@mes_hl_pur@" + PLACED + "</col>"));

        assertTrue(tracker.isActive());
        assertFalse(tracker.isExpiringSoon(0));
    }

    @Test
    public void ignoresUnrelatedMessagesAndNonGameMessages() {
        MarkOfDarknessTracker tracker = new TestTracker();
        tracker.onChatMessage(new ChatMessage(null, ChatMessageType.PUBLICCHAT, "", PLACED, "", 0));
        tracker.onChatMessage(gameMessage("Your defence against Arceuus magic has been strengthened."));
        tracker.onChatMessage(gameMessage(PLACED + " This is not the cast message."));
        assertFalse(tracker.isActive());

        tracker.onChatMessage(gameMessage(PLACED));
        tracker.onChatMessage(new ChatMessage(null, ChatMessageType.PUBLICCHAT, "", EXPIRING, "", 0));
        tracker.onChatMessage(new ChatMessage(null, ChatMessageType.PUBLICCHAT, "", FADED, "", 0));
        tracker.onChatMessage(gameMessage("Your Ward of Arceuus has expired."));
        assertTrue(tracker.isActive());
        assertFalse(tracker.isExpiringSoon(0));
    }

    @Test
    public void warningAndExpiryCannotActivateAnUntrackedMark() {
        MarkOfDarknessTracker tracker = new TestTracker();

        tracker.onChatMessage(gameMessage("@mes_hl_pur@" + EXPIRING + "</col>"));
        assertFalse(tracker.isActive());
        assertFalse(tracker.isExpiringSoon(0));

        tracker.onChatMessage(gameMessage("@mes_hl_pur@" + FADED + "</col>"));
        assertFalse(tracker.isActive());
    }

    @Test
    public void durationUsesThreeTicksPerRealMagicLevelAndPurgingStaffMultiplier() {
        assertEquals(183, MarkOfDarknessTracker.estimateDurationTicks(61, false));
        assertEquals(297, MarkOfDarknessTracker.estimateDurationTicks(99, false));
        assertEquals(1485, MarkOfDarknessTracker.estimateDurationTicks(99, true));
    }

    private static ChatMessage gameMessage(String message) {
        return new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", message, "", 0);
    }

    private static final class TestTracker extends MarkOfDarknessTracker {
        @Override
        int getDurationTicks() {
            return estimateDurationTicks(99, false);
        }
    }
}
