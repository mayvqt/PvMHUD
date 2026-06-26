package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PotionBoostTracker implements ResettableTracker {
    @Inject
    private Client client;

    private final int[] boostedLevels = new int[CombatBoosts.TRACKED_SKILLS.length];
    private final int[] baseLevels = new int[CombatBoosts.TRACKED_SKILLS.length];
    private final int[] peakBoostAmounts = new int[CombatBoosts.TRACKED_SKILLS.length];
    private boolean initialised;

    @Subscribe
    public void onStatChanged(StatChanged event) {
        int index = CombatBoosts.indexOf(event.getSkill());
        if (index < 0) {
            return;
        }

        boostedLevels[index] = event.getBoostedLevel();
        baseLevels[index] = event.getLevel();
        updatePeak(index);
    }

    public CombatBoost boostFor(Skill skill) {
        initialiseIfNeeded();

        int index = CombatBoosts.indexOf(skill);
        if (index < 0) {
            return null;
        }

        return boostAt(index);
    }

    public CombatBoost boostAt(int index) {
        initialiseIfNeeded();
        return new CombatBoost(
                CombatBoosts.TRACKED_SKILLS[index],
                boostedLevels[index],
                baseLevels[index],
                peakBoostAmounts[index]
        );
    }

    @Override
    public void reset() {
        initialised = false;
        for (int i = 0; i < CombatBoosts.TRACKED_SKILLS.length; i++) {
            boostedLevels[i] = 0;
            baseLevels[i] = 0;
            peakBoostAmounts[i] = 0;
        }
    }

    private void initialiseIfNeeded() {
        if (initialised) {
            return;
        }

        for (int i = 0; i < CombatBoosts.TRACKED_SKILLS.length; i++) {
            Skill skill = CombatBoosts.TRACKED_SKILLS[i];
            boostedLevels[i] = client.getBoostedSkillLevel(skill);
            baseLevels[i] = client.getRealSkillLevel(skill);
            updatePeak(i);
        }
        initialised = true;
    }

    private void updatePeak(int index) {
        int boost = boostedLevels[index] - baseLevels[index];
        if (boost <= 0) {
            peakBoostAmounts[index] = 0;
            return;
        }

        peakBoostAmounts[index] = Math.max(peakBoostAmounts[index], boost);
    }
}
