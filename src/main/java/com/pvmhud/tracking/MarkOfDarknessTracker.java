package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class MarkOfDarknessTracker implements SpellStateTracker, ResettableTracker {
    private boolean active;
    private boolean expiringSoon;

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String msg = Text.removeTags(event.getMessage()).toLowerCase(Locale.ENGLISH);

        if (msg.contains("mark of darkness upon yourself")) {
            active = true;
            expiringSoon = false;
        } else if (msg.contains("mark of darkness is about to run out")) {
            expiringSoon = true;
        } else if (msg.contains("mark of darkness has faded")) {
            active = false;
            expiringSoon = false;
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isOnCooldown() {
        return false;
    }

    @Override
    public boolean isExpiringSoon(int windowSeconds) {
        return active && expiringSoon;
    }

    @Override
    public void reset() {
        active = false;
        expiringSoon = false;
    }
}