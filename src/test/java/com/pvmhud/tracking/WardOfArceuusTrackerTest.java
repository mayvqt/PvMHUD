package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WardOfArceuusTrackerTest {
    @Test
    public void durationUsesSixTenthsOfRealMagicLevel() {
        assertEquals(TimeConstants.secondsToNanos(59.4d), WardOfArceuusTracker.estimateDurationNanos(99));
        assertEquals(0L, WardOfArceuusTracker.estimateDurationNanos(-1));
    }

    @Test
    public void clearsActiveWardAcrossExpiryMessageFormats() {
        String[] messages = {
                "Your Ward of Arceuus has expired.",
                "<col=0000b2>Your Ward of Arceuus has expired.</col>",
                "@mes_hl_blu@Your Ward of Arceuus has expired.</col>"
        };

        for (String message : messages) {
            WardOfArceuusTracker tracker = new WardOfArceuusTracker();
            tracker.markActive(TimeConstants.secondsToNanos(60));

            tracker.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", message, "", 0));

            assertFalse(message, tracker.hasActiveEffect());
            assertEquals(message, "", tracker.getDisplayText());
        }
    }

    @Test
    public void ignoresUnrelatedMessagesAndNonGameMessages() {
        WardOfArceuusTracker tracker = new WardOfArceuusTracker();
        tracker.markActive(TimeConstants.secondsToNanos(60));

        tracker.onChatMessage(new ChatMessage(null, ChatMessageType.PUBLICCHAT, "",
                "@mes_hl_blu@Your Ward of Arceuus has expired.</col>", "", 0));
        tracker.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "",
                "Your Mark of Darkness has faded away.", "", 0));

        assertTrue(tracker.hasActiveEffect());
    }
}
