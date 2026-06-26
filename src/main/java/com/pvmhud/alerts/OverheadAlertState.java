package com.pvmhud.alerts;

import com.pvmhud.tracking.CombatBoosts;
import com.pvmhud.tracking.TimedPotionType;
import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OverheadAlertState {
    private int previousHitpoints = -1;
    private int previousPrayer = -1;
    private int previousSpec = -1;
    private final int[] previousCombatBoosts = new int[Skill.values().length];
    private final int[] peakCombatBoosts = new int[Skill.values().length];
    private final boolean[] previousTimedPotionActive = new boolean[TimedPotionType.values().length];
    private final boolean[] timedPotionExpiringAlerted = new boolean[TimedPotionType.values().length];
    private long lastPotionAlertNanos;
    private boolean previousSpecReady;
    private boolean baselineReady;

    @Inject
    private Client client;

    public void reset() {
        previousHitpoints = -1;
        previousPrayer = -1;
        previousSpec = -1;
        for (int i = 0; i < previousCombatBoosts.length; i++) {
            previousCombatBoosts[i] = Integer.MIN_VALUE;
            peakCombatBoosts[i] = 0;
        }
        for (int i = 0; i < previousTimedPotionActive.length; i++) {
            previousTimedPotionActive[i] = false;
            timedPotionExpiringAlerted[i] = false;
        }
        lastPotionAlertNanos = 0L;
        previousSpecReady = false;
        baselineReady = false;
    }

    public void captureBaseline(int specPercent) {
        previousHitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
        previousPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        previousSpec = specPercent;
        for (Skill skill : CombatBoosts.TRACKED_SKILLS) {
            int boost = client.getBoostedSkillLevel(skill) - client.getRealSkillLevel(skill);
            previousCombatBoosts[skill.ordinal()] = boost;
            peakCombatBoosts[skill.ordinal()] = Math.max(0, boost);
        }
        previousSpecReady = true;
        baselineReady = true;
    }

    boolean isBaselineReady() {
        return baselineReady;
    }

    int getPreviousHitpoints() {
        return previousHitpoints;
    }

    void setPreviousHitpoints(int previousHitpoints) {
        this.previousHitpoints = previousHitpoints;
    }

    int getPreviousPrayer() {
        return previousPrayer;
    }

    void setPreviousPrayer(int previousPrayer) {
        this.previousPrayer = previousPrayer;
    }

    int getPreviousSpec() {
        return previousSpec;
    }

    void setPreviousSpec(int previousSpec) {
        this.previousSpec = previousSpec;
    }

    boolean isPreviousSpecReady() {
        return previousSpecReady;
    }

    void setPreviousSpecReady(boolean previousSpecReady) {
        this.previousSpecReady = previousSpecReady;
    }

    int getPreviousCombatBoost(Skill skill) {
        return previousCombatBoosts[skill.ordinal()];
    }

    void setPreviousCombatBoost(Skill skill, int boost) {
        previousCombatBoosts[skill.ordinal()] = boost;
    }

    int getPeakCombatBoost(Skill skill) {
        return peakCombatBoosts[skill.ordinal()];
    }

    void setPeakCombatBoost(Skill skill, int boost) {
        peakCombatBoosts[skill.ordinal()] = Math.max(0, boost);
    }

    boolean wasTimedPotionActive(TimedPotionType type) {
        return previousTimedPotionActive[type.ordinal()];
    }

    void setTimedPotionActive(TimedPotionType type, boolean active) {
        previousTimedPotionActive[type.ordinal()] = active;
    }

    boolean isTimedPotionExpiringAlerted(TimedPotionType type) {
        return timedPotionExpiringAlerted[type.ordinal()];
    }

    void setTimedPotionExpiringAlerted(TimedPotionType type, boolean alerted) {
        timedPotionExpiringAlerted[type.ordinal()] = alerted;
    }

    long getLastPotionAlertNanos() {
        return lastPotionAlertNanos;
    }

    void setLastPotionAlertNanos(long lastPotionAlertNanos) {
        this.lastPotionAlertNanos = lastPotionAlertNanos;
    }
}
