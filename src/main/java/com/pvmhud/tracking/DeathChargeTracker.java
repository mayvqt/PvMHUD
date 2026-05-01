package com.pvmhud.tracking;

import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class DeathChargeTracker extends CachedVarbitTracker implements ResettableTracker {
    private static final long DEATH_CHARGE_DURATION_NANOS = TimeConstants.secondsToNanos(60);

    private int cachedActive;
    private int cachedCooldown;
    private long activatedAtNanos;

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String msg = Text.removeTags(event.getMessage()).toLowerCase(Locale.ENGLISH);

        if (msg.contains("upon the death of your next foe")
                || msg.contains("upon the death of your next two foes")) {
            cachedActive = 1;
            activatedAtNanos = System.nanoTime();
        } else if (msg.contains("your special attack energy has been restored")) {
            cachedActive = 0;
        }
    }

    @Override
    public boolean isActive() {
        syncIfNeeded();
        return cachedActive > 0;
    }

    @Override
    public boolean isOnCooldown() {
        syncIfNeeded();
        return cachedCooldown > 0;
    }

    @Override
    public boolean isReady() {
        syncIfNeeded();
        return cachedActive == 0 && cachedCooldown == 0;
    }

    @Override
    public boolean isExpiringSoon(int windowSeconds) {
        syncIfNeeded();

        if (activatedAtNanos != 0L) {
            long remaining = DEATH_CHARGE_DURATION_NANOS - elapsedSinceActivation();
            return remaining > 0L && remaining <= TimeConstants.secondsToNanos(windowSeconds);
        }

        return false;
    }

    @Override
    protected void performSync() {
        int previousActive = cachedActive;

        cachedActive = client.getVarbitValue(GameStateIds.DEATH_CHARGE_ACTIVE);
        cachedCooldown = client.getVarbitValue(GameStateIds.DEATH_CHARGE_COOLDOWN);

        if (cachedActive > 0 && previousActive == 0 && activatedAtNanos == 0L) {
            activatedAtNanos = System.nanoTime();
        } else if (activatedAtNanos != 0L
                && elapsedSinceActivation() >= DEATH_CHARGE_DURATION_NANOS) {
            activatedAtNanos = 0L;
        }
    }

    @Override
    public void reset() {
        cachedActive = 0;
        cachedCooldown = 0;
        activatedAtNanos = 0L;
        invalidateCache();
    }

    private long elapsedSinceActivation() {
        return System.nanoTime() - activatedAtNanos;
    }
}
