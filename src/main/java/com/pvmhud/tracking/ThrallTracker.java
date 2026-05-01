package com.pvmhud.tracking;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;

@Singleton
public class ThrallTracker implements SpellStateTracker, ResettableTracker {
    private static final int THRALL_SPAWN_DELAY_TICKS = 4;
    private static final int NO_TRACKED_THRALL = -1;
    private static final long DUPLICATE_SUMMON_WINDOW_NANOS =
            TimeConstants.GAME_TICK_MILLIS * THRALL_SPAWN_DELAY_TICKS * TimeConstants.NS_PER_MS;

    @Inject
    private Client client;
    @Inject
    private PvMHUDConfig config;

    private long lastSummonNanos;
    private boolean wasCooldownActive;
    private int cachedCooldown;
    private long lastVarbitSyncMs;
    private int spawnGraceUntilTick;
    private int trackedThrallIndex = NO_TRACKED_THRALL;

    private long cachedDurationNanos;

    private boolean isThrallName(String name) {
        return name != null && name.toLowerCase(Locale.ENGLISH).contains("thrall");
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (npc == null) {
            return;
        }

        String name = Text.removeTags(npc.getName());
        if (!isThrallName(name)) {
            return;
        }

        if (trackedThrallIndex != NO_TRACKED_THRALL || !isSpawnGracePeriodActive()) {
            return;
        }

        trackedThrallIndex = npc.getIndex();
        spawnGraceUntilTick = 0;
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        if (npc != null && npc.getIndex() == trackedThrallIndex) {
            clearActiveSummonEvidence();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE
                && event.getType() != ChatMessageType.SPAM) {
            return;
        }

        String msg = Text.removeTags(event.getMessage()).toLowerCase(Locale.ENGLISH);

        if (isThrallSummonMessage(msg)) {
            markSummoned();
        } else if (isThrallReturnMessage(msg)) {
            clearActiveSummonEvidence();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (spawnGraceUntilTick > 0 && client.getTickCount() > spawnGraceUntilTick) {
            spawnGraceUntilTick = 0;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() != GameStateIds.RESURRECT_THRALL_COOLDOWN) {
            return;
        }

        lastVarbitSyncMs = System.currentTimeMillis();
        cachedCooldown = event.getValue();

        boolean cooldownActive = cachedCooldown > 0;

        if (cooldownActive && !wasCooldownActive) {
            markSummoned();
        }

        wasCooldownActive = cooldownActive;
    }

    @Override
    public boolean isActive() {
        syncVarbitsIfNeeded();
        return isSummonTimerActive();
    }

    @Override
    public boolean isOnCooldown() {
        syncVarbitsIfNeeded();
        return cachedCooldown > 0;
    }

    @Override
    public boolean isExpiringSoon(int soonWindowSeconds) {
        if (!isActive() || lastSummonNanos == 0L) {
            return false;
        }

        long now = System.nanoTime();
        long remaining = cachedDurationNanos - (now - lastSummonNanos);

        return remaining > 0L
                && remaining <= TimeConstants.secondsToNanos(soonWindowSeconds);
    }

    @Override
    public void reset() {
        lastSummonNanos = 0L;
        wasCooldownActive = false;
        cachedCooldown = 0;
        lastVarbitSyncMs = 0L;
        spawnGraceUntilTick = 0;
        trackedThrallIndex = NO_TRACKED_THRALL;
        cachedDurationNanos = 0L;
    }

    private boolean isThrallSummonMessage(String msg) {
        return msg.contains("you summon")
                && msg.contains("thrall");
    }

    private boolean isThrallReturnMessage(String msg) {
        return msg.contains("thrall")
                && msg.contains("return")
                && msg.contains("grave");
    }

    private void markSummoned() {
        long now = System.nanoTime();
        if (lastSummonNanos > 0L && now - lastSummonNanos <= DUPLICATE_SUMMON_WINDOW_NANOS) {
            startSpawnGracePeriod();
            return;
        }

        trackedThrallIndex = NO_TRACKED_THRALL;
        lastSummonNanos = now;
        cachedDurationNanos = estimateThrallDurationNanos();
        startSpawnGracePeriod();
    }

    private void startSpawnGracePeriod() {
        spawnGraceUntilTick = client.getTickCount() + THRALL_SPAWN_DELAY_TICKS;
    }

    private boolean isSpawnGracePeriodActive() {
        return spawnGraceUntilTick > 0 && client.getTickCount() <= spawnGraceUntilTick;
    }

    private void clearActiveSummonEvidence() {
        lastSummonNanos = 0L;
        spawnGraceUntilTick = 0;
        trackedThrallIndex = NO_TRACKED_THRALL;
        cachedDurationNanos = 0L;
    }

    private void syncVarbitsIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastVarbitSyncMs >= TimeConstants.CACHE_SYNC_INTERVAL_MS) {
            syncVarbits();
        }
    }

    private void syncVarbits() {
        lastVarbitSyncMs = System.currentTimeMillis();
        cachedCooldown = client.getVarbitValue(GameStateIds.RESURRECT_THRALL_COOLDOWN);
    }

    private boolean isSummonTimerActive() {
        return lastSummonNanos > 0L
                && cachedDurationNanos > 0L
                && System.nanoTime() - lastSummonNanos < cachedDurationNanos;
    }

    private long estimateThrallDurationNanos() {
        int magic = client.getBoostedSkillLevel(Skill.MAGIC);
        double seconds = magic * 0.6d;

        if (config.masterCombatAchievements()) {
            seconds *= 2.0d;
        }

        seconds += THRALL_SPAWN_DELAY_TICKS * (TimeConstants.GAME_TICK_MILLIS / (double) TimeConstants.MS_PER_SECOND);

        return TimeConstants.secondsToNanos(seconds);
    }
}
