package com.pvmhud.tracking;

import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.function.Consumer;

@Singleton
public class TimedPotionTracker extends CachedVarbitTracker {
    private static final TimedPotionType[] TYPES = TimedPotionType.values();

    private final int[] remainingTicks = new int[TYPES.length];

    public void forEachRaidPotion(Consumer<TimedPotionEffect> consumer) {
        forEachPotion(true, consumer);
    }

    public void forEachDivinePotion(Consumer<TimedPotionEffect> consumer) {
        forEachPotion(false, consumer);
    }

    public void forEachPotion(Consumer<TimedPotionEffect> consumer) {
        forEachPotion(null, consumer);
    }

    private void forEachPotion(Boolean raidPotion, Consumer<TimedPotionEffect> consumer) {
        syncIfNeeded();
        for (TimedPotionType type : TYPES) {
            if (raidPotion != null && type.isRaidPotion() != raidPotion) {
                continue;
            }

            if (isSuppressedByCombo(type)) {
                continue;
            }

            consumer.accept(new TimedPotionEffect(type, remainingTicks[type.ordinal()]));
        }
    }

    @Override
    protected void sync() {
        for (TimedPotionType type : TYPES) {
            remainingTicks[type.ordinal()] = client.getVarbitValue(type.getTimerVarbitId());
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        updateTimer(event.getVarbitId(), event.getValue());
    }

    public void updateTimer(int varbitId, int value) {
        for (TimedPotionType type : TYPES) {
            if (type.getTimerVarbitId() == varbitId) {
                remainingTicks[type.ordinal()] = Math.max(0, value);
                return;
            }
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < remainingTicks.length; i++) {
            remainingTicks[i] = 0;
        }
        invalidateCache();
    }

    public static boolean isPotionTimerVarbit(int varbitId) {
        for (TimedPotionType type : TYPES) {
            if (type.getTimerVarbitId() == varbitId) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuppressedByCombo(TimedPotionType type) {
        int remaining = remainingTicks[type.ordinal()];
        if (remaining <= 0 || type.isComboPotion()) {
            return false;
        }

        for (TimedPotionType combo : TYPES) {
            if (!combo.isComboPotion() || combo == type || combo.isRaidPotion() != type.isRaidPotion()) {
                continue;
            }

            if (remainingTicks[combo.ordinal()] >= remaining && type.sharesAffectedSkill(combo)) {
                return true;
            }
        }

        return false;
    }
}
