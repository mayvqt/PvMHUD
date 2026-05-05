package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HpTracker implements ResettableTracker {
    @Inject
    private Client client;

    private int currentHitpoints = -1;

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (event.getSkill() == Skill.HITPOINTS) {
            currentHitpoints = event.getBoostedLevel();
        }
    }

    public int getCurrentHp() {
        if (currentHitpoints < 0) {
            currentHitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
        }
        return currentHitpoints;
    }

    @Override
    public void reset() {
        currentHitpoints = -1;
    }
}
