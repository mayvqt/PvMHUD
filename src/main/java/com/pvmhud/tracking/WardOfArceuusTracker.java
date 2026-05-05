package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;

@Singleton
public class WardOfArceuusTracker extends BaseTimedSpellTracker {
    private static final String WARD_EXPIRED_MESSAGE =
            "your ward of arceuus has expired.";

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() != GameStateIds.WARD_OF_ARCEUUS_COOLDOWN) {
            return;
        }

        int cooldownTicks = event.getValue();
        setCooldownActive(cooldownTicks > 0);

        if (cooldownTicks == 1) {
            markActive(estimateDurationNanos());
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = Text.standardize(event.getMessage());

        if (WARD_EXPIRED_MESSAGE.equals(message)) {
            clearActive();
        }
    }

    @Override
    protected void sync() {
        int cooldownTicks = client.getVarbitValue(GameStateIds.WARD_OF_ARCEUUS_COOLDOWN);
        setCooldownActive(cooldownTicks > 0);
    }

    private long estimateDurationNanos() {
        double seconds = client.getBoostedSkillLevel(Skill.MAGIC) * 0.6d;
        return TimeConstants.secondsToNanos(seconds);
    }
}