package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class DeathChargeTracker extends BaseTimedSpellTracker {
    private static final long DEATH_CHARGE_DURATION_NANOS = TimeConstants.secondsToNanos(60);

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = Text.removeTags(event.getMessage()).toLowerCase(Locale.ENGLISH);

        if (message.contains("upon the death of your next foe")
                || message.contains("upon the death of your next two foes")) {
            markActive(DEATH_CHARGE_DURATION_NANOS);
        } else if (message.contains("your special attack energy has been restored")) {
            clearActive();
            setCooldownActive(true);
        }
    }

    @Override
    protected void sync() {
        int active = client.getVarbitValue(GameStateIds.DEATH_CHARGE_ACTIVE);
        int cooldown = client.getVarbitValue(GameStateIds.DEATH_CHARGE_COOLDOWN);

        if (active > 0 && !isActive()) {
            markActive(DEATH_CHARGE_DURATION_NANOS);
        }

        if (active <= 0 && getActiveStartedAtNanos() > 0L
                && System.nanoTime() - getActiveStartedAtNanos() >= DEATH_CHARGE_DURATION_NANOS) {
            clearActive();
        }

        setCooldownActive(cooldown > 0);
    }
}
