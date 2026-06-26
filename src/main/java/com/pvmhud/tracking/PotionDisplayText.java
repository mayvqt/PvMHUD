package com.pvmhud.tracking;

import net.runelite.api.Skill;

public final class PotionDisplayText {
    private PotionDisplayText() {
    }

    public static String boostLabel(CombatBoost boost) {
        return skillPrefix(boost.getSkill()) + " +" + boost.getBoostAmount()
                + " " + boost.getRemainingBoostPercent() + "%";
    }

    public static String boostIconText(CombatBoost boost) {
        return "+" + boost.getBoostAmount();
    }

    public static String timedPotionLabel(TimedPotionEffect effect) {
        return effect.getType().getShortName() + " " + formatTicks(effect.getRemainingTicks());
    }

    public static String timedPotionIconText(TimedPotionEffect effect) {
        return formatTicks(effect.getRemainingTicks());
    }

    public static String formatBoostMessage(String template, CombatBoost boost, int thresholdPercent) {
        return template
                .replace("{skill}", boost.getSkill().getName())
                .replace("{level}", Integer.toString(boost.getBoostedLevel()))
                .replace("{base}", Integer.toString(boost.getBaseLevel()))
                .replace("{boost}", Integer.toString(boost.getBoostAmount()))
                .replace("{peak}", Integer.toString(boost.getPeakBoostAmount()))
                .replace("{percent}", Integer.toString(boost.getRemainingBoostPercent()))
                .replace("{threshold}", Integer.toString(thresholdPercent));
    }

    public static String formatTimedPotionMessage(String template, TimedPotionEffect effect) {
        return template
                .replace("{potion}", effect.getType().getDisplayName())
                .replace("{time}", formatTicks(effect.getRemainingTicks()))
                .replace("{ticks}", Integer.toString(effect.getRemainingTicks()));
    }

    public static String formatTicks(int ticks) {
        int totalSeconds = (int) Math.ceil(ticks * TimeConstants.GAME_TICK_MILLIS / (double) TimeConstants.MS_PER_SECOND);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static String skillPrefix(Skill skill) {
        switch (skill) {
            case ATTACK:
                return "Atk";
            case STRENGTH:
                return "Str";
            case DEFENCE:
                return "Def";
            case RANGED:
                return "Rng";
            case MAGIC:
                return "Mag";
            default:
                return skill.getName();
        }
    }
}
