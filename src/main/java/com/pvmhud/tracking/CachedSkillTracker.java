package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;

abstract class CachedSkillTracker implements ResettableTracker {
    @Inject
    private Client client;

    private final Skill skill;
    private int boostedLevel = -1;

    CachedSkillTracker(Skill skill) {
        this.skill = skill;
    }

    @Subscribe
    public final void onStatChanged(StatChanged event) {
        if (event.getSkill() == skill) {
            boostedLevel = event.getBoostedLevel();
        }
    }

    protected final int getBoostedLevel() {
        if (boostedLevel < 0) {
            boostedLevel = client.getBoostedSkillLevel(skill);
        }
        return boostedLevel;
    }

    @Override
    public final void reset() {
        boostedLevel = -1;
    }
}
