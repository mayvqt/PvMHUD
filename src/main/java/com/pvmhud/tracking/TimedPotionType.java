package com.pvmhud.tracking;

import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

public enum TimedPotionType {
    OVERLOAD("Overload", "Ovl", ItemID.RAIDS_VIAL_OVERLOAD_4, VarbitID.RAIDS_OVERLOAD_TIMER, true,
            Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC),
    SALTS("Salts", "Salt", ItemID.BLUE_SALT, VarbitID.TOA_MIDRAIDLOOT_STATS_TIMER, true,
            Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC),
    DIVINE_SUPER_ATTACK("Divine attack", "DAtk", ItemID._4DOSEDIVINEATTACK, VarbitID.DIVINEATTACK_POTION_TIME, false,
            Skill.ATTACK),
    DIVINE_SUPER_STRENGTH("Divine strength", "DStr", ItemID._4DOSEDIVINESTRENGTH, VarbitID.DIVINESTRENGTH_POTION_TIME, false,
            Skill.STRENGTH),
    DIVINE_SUPER_DEFENCE("Divine defence", "DDef", ItemID._4DOSEDIVINEDEFENCE, VarbitID.DIVINEDEFENCE_POTION_TIME, false,
            Skill.DEFENCE),
    DIVINE_RANGING("Divine ranging", "DRng", ItemID._4DOSEDIVINERANGE, VarbitID.DIVINERANGE_POTION_TIME, false,
            Skill.RANGED),
    DIVINE_MAGIC("Divine magic", "DMag", ItemID._4DOSEDIVINEMAGIC, VarbitID.DIVINEMAGIC_POTION_TIME, false,
            Skill.MAGIC),
    DIVINE_SUPER_COMBAT("Divine combat", "DCom", ItemID._4DOSEDIVINECOMBAT, VarbitID.DIVINECOMBAT_POTION_TIME, false,
            Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE),
    DIVINE_BASTION("Divine bastion", "DBas", ItemID._4DOSEDIVINEBASTION, VarbitID.DIVINEBASTION_POTION_TIME, false,
            Skill.RANGED, Skill.DEFENCE),
    DIVINE_BATTLEMAGE("Divine battlemage", "DBat", ItemID._4DOSEDIVINEBATTLEMAGE, VarbitID.DIVINEBATTLEMAGE_POTION_TIME, false,
            Skill.MAGIC, Skill.DEFENCE);

    private final String displayName;
    private final String shortName;
    private final int itemId;
    private final int timerVarbitId;
    private final boolean raidPotion;
    private final Skill[] affectedSkills;

    TimedPotionType(String displayName, String shortName, int itemId, int timerVarbitId, boolean raidPotion, Skill... affectedSkills) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.itemId = itemId;
        this.timerVarbitId = timerVarbitId;
        this.raidPotion = raidPotion;
        this.affectedSkills = affectedSkills.clone();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public int getItemId() {
        return itemId;
    }

    public int getTimerVarbitId() {
        return timerVarbitId;
    }

    public boolean isRaidPotion() {
        return raidPotion;
    }

    public boolean isComboPotion() {
        return this == OVERLOAD
                || this == SALTS
                || this == DIVINE_SUPER_COMBAT
                || this == DIVINE_BASTION
                || this == DIVINE_BATTLEMAGE;
    }

    boolean sharesAffectedSkill(TimedPotionType other) {
        for (Skill skill : affectedSkills) {
            for (Skill otherSkill : other.affectedSkills) {
                if (skill == otherSkill) {
                    return true;
                }
            }
        }
        return false;
    }
}
