package com.pvmhud.tracking;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Skill;

public final class CombatBoosts {
    public static final Skill[] TRACKED_SKILLS = {
            Skill.ATTACK,
            Skill.STRENGTH,
            Skill.DEFENCE,
            Skill.RANGED,
            Skill.MAGIC
    };

    private CombatBoosts() {
    }

    public static boolean isTracked(Skill skill) {
        return indexOf(skill) >= 0;
    }

    public static int indexOf(Skill skill) {
        for (int i = 0; i < TRACKED_SKILLS.length; i++) {
            if (TRACKED_SKILLS[i] == skill) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isEnabled(PvMHUDConfig config, Skill skill) {
        switch (skill) {
            case ATTACK:
                return config.showAttackBoost();
            case STRENGTH:
                return config.showStrengthBoost();
            case DEFENCE:
                return config.showDefenceBoost();
            case RANGED:
                return config.showRangedBoost();
            case MAGIC:
                return config.showMagicBoost();
            default:
                return false;
        }
    }
}
