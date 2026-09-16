package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;

@Singleton
public class WardOfArceuusTracker extends BaseTimedSpellTracker {
    private static final String WARD_EXPIRED_MESSAGE =
            "your ward of arceuus has expired.";

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() != VarbitID.ARCEUUS_WARD_COOLDOWN) {
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

        if (message.endsWith(WARD_EXPIRED_MESSAGE)) {
            clearActive();
        }
    }

    @Override
    protected void sync() {
        int cooldownTicks = client.getVarbitValue(VarbitID.ARCEUUS_WARD_COOLDOWN);
        setCooldownActive(cooldownTicks > 0);
    }

    private long estimateDurationNanos() {
        return estimateDurationNanos(client.getRealSkillLevel(Skill.MAGIC));
    }

    static long estimateDurationNanos(int realMagicLevel) {
        double seconds = Math.max(0, realMagicLevel) * 0.6d;
        return TimeConstants.secondsToNanos(seconds);
    }
}
