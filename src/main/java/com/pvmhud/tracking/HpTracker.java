package com.pvmhud.tracking;

import net.runelite.api.Skill;

import javax.inject.Singleton;

@Singleton
public class HpTracker extends CachedSkillTracker {
    public HpTracker() {
        super(Skill.HITPOINTS);
    }

    public int getCurrentHp() {
        return getBoostedLevel();
    }
}
