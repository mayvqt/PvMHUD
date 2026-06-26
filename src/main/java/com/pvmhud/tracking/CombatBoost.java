package com.pvmhud.tracking;

import net.runelite.api.Skill;

public final class CombatBoost {
    private final Skill skill;
    private final int boostedLevel;
    private final int baseLevel;
    private final int peakBoostAmount;

    public CombatBoost(Skill skill, int boostedLevel, int baseLevel, int peakBoostAmount) {
        this.skill = skill;
        this.boostedLevel = boostedLevel;
        this.baseLevel = baseLevel;
        this.peakBoostAmount = Math.max(0, peakBoostAmount);
    }

    public Skill getSkill() {
        return skill;
    }

    public int getBoostedLevel() {
        return boostedLevel;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public int getBoostAmount() {
        return boostedLevel - baseLevel;
    }

    public int getPeakBoostAmount() {
        return peakBoostAmount;
    }

    public int getRemainingBoostPercent() {
        if (peakBoostAmount <= 0 || getBoostAmount() <= 0) {
            return 0;
        }

        return Math.min(100, (int) Math.ceil(getBoostAmount() * 100.0 / peakBoostAmount));
    }

    public boolean isAtOrBelowPercentThreshold(int thresholdPercent) {
        if (!isBoosted() || peakBoostAmount <= 0) {
            return false;
        }

        int thresholdBoost = (int) Math.ceil(peakBoostAmount * Math.max(0, thresholdPercent) / 100.0);
        return getBoostAmount() <= Math.max(1, thresholdBoost);
    }

    public boolean isBoosted() {
        return boostedLevel > baseLevel;
    }
}
