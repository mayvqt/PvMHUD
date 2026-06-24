package com.pvmhud.tracking;

import net.runelite.api.Skill;

import javax.inject.Singleton;

@Singleton
public class PrayerTracker extends CachedSkillTracker {
    public PrayerTracker() {
        super(Skill.PRAYER);
    }

    public int getCurrentPrayer() {
        return getBoostedLevel();
    }
}
