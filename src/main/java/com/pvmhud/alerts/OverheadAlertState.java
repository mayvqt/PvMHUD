package com.pvmhud.alerts;

import net.runelite.api.Client;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OverheadAlertState {
    private int previousHitpoints = -1;
    private int previousPrayer = -1;
    private int previousSpec = -1;
    private boolean previousSpecReady;
    private boolean baselineReady;

    @Inject
    private Client client;

    public void reset() {
        previousHitpoints = -1;
        previousPrayer = -1;
        previousSpec = -1;
        previousSpecReady = false;
        baselineReady = false;
    }

    public void captureBaseline() {
        previousHitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
        previousPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        previousSpec = -1;
        previousSpecReady = false;
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
}
