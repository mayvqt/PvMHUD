package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PrayerTracker implements ResettableTracker {
    @Inject
    private Client client;

    private int currentPrayer = -1;

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (event.getSkill() == Skill.PRAYER) {
            currentPrayer = event.getBoostedLevel();
        }
    }

    public int getCurrentPrayer() {
        if (currentPrayer < 0) {
            currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        }
        return currentPrayer;
    }

    @Override
    public void reset() {
        currentPrayer = -1;
    }
}
