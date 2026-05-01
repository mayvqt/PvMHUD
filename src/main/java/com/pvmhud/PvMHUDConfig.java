package com.pvmhud;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import java.awt.Color;

@ConfigGroup("pvmhud")
public interface PvMHUDConfig extends Config {
    Color DEFAULT_READY = new Color(222, 205, 164);
    Color DEFAULT_COOLDOWN = new Color(104, 96, 84);
    Color DEFAULT_DANGER = new Color(224, 42, 42);
    Color DEFAULT_WARNING = new Color(255, 178, 48);

    @ConfigSection(
            name = "General",
            description = "Choose the HUD style and visible indicators.",
            position = 0
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Text and icon HUD",
            description = "Configure the Text and Game icons HUD styles.",
            position = 1
    )
    String textIconSection = "textIcon";

    @ConfigSection(
            name = "Bars and compact HUDs",
            description = "Configure Bars, Chips, Orbs, and Stack HUD styles.",
            position = 2
    )
    String barStyleSection = "barStyle";

    @ConfigSection(
            name = "Overhead alerts",
            description = "Configure low Hitpoints and Prayer overhead messages.",
            position = 3
    )
    String alertSection = "alerts";

    @ConfigSection(
            name = "Spell timing",
            description = "Configure indicator timing and warnings.",
            position = 4
    )
    String timingSection = "timing";

    @ConfigSection(
            name = "Stat thresholds",
            description = "Configure HP, Prayer, and special attack thresholds.",
            position = 5
    )
    String thresholdSection = "thresholds";

    @ConfigSection(
            name = "Background",
            description = "Configure the HUD background.",
            position = 6
    )
    String backgroundSection = "background";

    @ConfigSection(
            name = "Stat colors",
            description = "Set HP, Prayer, and special attack colors.",
            position = 7
    )
    String statColorSection = "statColors";

    @ConfigSection(
            name = "Spell state colors",
            description = "Set shared ready, cooldown, expiry, and flash colors.",
            position = 8
    )
    String spellStateColorSection = "spellStateColors";

    @ConfigSection(
            name = "Active spell colors",
            description = "Set active colors for each tracked spell.",
            position = 9
    )
    String activeSpellColorSection = "activeSpellColors";

    @ConfigItem(keyName = "hudStyle", name = "Style", description = "Choose the HUD display style.", position = 0, section = generalSection)
    default HudStyle hudStyle() {
        return HudStyle.TEXT;
    }

    @ConfigItem(keyName = "showThrall", name = "Thrall", description = "Show the Resurrect Thrall indicator.", position = 1, section = generalSection)
    default boolean showThrall() {
        return true;
    }

    @ConfigItem(keyName = "showVengeance", name = "Vengeance", description = "Show the Vengeance indicator.", position = 2, section = generalSection)
    default boolean showVengeance() {
        return true;
    }

    @ConfigItem(keyName = "showDeathCharge", name = "Death Charge", description = "Show the Death Charge indicator.", position = 3, section = generalSection)
    default boolean showDeathCharge() {
        return true;
    }

    @ConfigItem(keyName = "showMarkOfDarkness", name = "Mark of Darkness", description = "Show the Mark of Darkness indicator.", position = 4, section = generalSection)
    default boolean showMarkOfDarkness() {
        return true;
    }

    @ConfigItem(keyName = "showCorruption", name = "Corruption", description = "Show the Corruption indicator.", position = 5, section = generalSection)
    default boolean showCorruption() {
        return true;
    }

    @ConfigItem(keyName = "showWardOfArceuus", name = "Ward of Arceuus", description = "Show the Ward of Arceuus indicator.", position = 6, section = generalSection)
    default boolean showWardOfArceuus() {
        return true;
    }

    @ConfigItem(keyName = "showImbuedHeart", name = "Heart", description = "Show the Imbued or Saturated Heart cooldown indicator.", position = 7, section = generalSection)
    default boolean showHeart() {
        return true;
    }

    @ConfigItem(keyName = "showHp", name = "Hitpoints", description = "Show current Hitpoints.", position = 8, section = generalSection)
    default boolean showHp() {
        return true;
    }

    @ConfigItem(keyName = "showPrayer", name = "Prayer", description = "Show current Prayer.", position = 9, section = generalSection)
    default boolean showPrayer() {
        return true;
    }

    @ConfigItem(keyName = "showSpec", name = "Special attack", description = "Show special attack energy.", position = 10, section = generalSection)
    default boolean showSpec() {
        return true;
    }

    @ConfigItem(keyName = "masterCombatAchievements", name = "Master CA thralls",
            description = "Use doubled thrall duration for the Master Combat Achievement reward.",
            position = 0, section = timingSection)
    default boolean masterCombatAchievements() {
        return false;
    }

    @ConfigItem(keyName = "showInactiveSpells", name = "Keep inactive spells",
            description = "Briefly keep spell indicators visible after they become ready.",
            position = 1, section = timingSection)
    default boolean showInactiveSpells() {
        return true;
    }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "inactiveSpellTimeoutSeconds", name = "Inactive timeout",
            description = "Seconds to keep ready spell indicators visible after use.",
            position = 2, section = timingSection)
    default int inactiveSpellTimeoutSeconds() {
        return 10;
    }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "spellExpiringSoonSeconds", name = "Expiry warning",
            description = "Seconds before a spell expires to use the expiring color.",
            position = 3, section = timingSection)
    default int spellExpiringSoonSeconds() {
        return 8;
    }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "hpLowThreshold", name = "Low HP",
            description = "Use the low HP color at or below this Hitpoints value.", position = 0, section = thresholdSection)
    default int hpLowThreshold() {
        return 30;
    }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "prayerLowThreshold", name = "Low Prayer",
            description = "Use the low Prayer color at or below this Prayer value.", position = 1, section = thresholdSection)
    default int prayerLowThreshold() {
        return 10;
    }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "specThreshold", name = "High special attack",
            description = "Use the high special attack color at or above this percentage.", position = 2, section = thresholdSection)
    default int specThreshold() {
        return 50;
    }

    @ConfigItem(keyName = "overheadThresholdAlerts", name = "Enabled",
            description = "Show an overhead message when Hitpoints or Prayer drops to its low threshold.",
            position = 0, section = alertSection)
    default boolean overheadThresholdAlerts() {
        return true;
    }

    @ConfigItem(keyName = "lowHpOverheadMessage", name = "Low HP",
            description = "Overhead message to show when Hitpoints drops to the low HP threshold.",
            position = 1, section = alertSection)
    default String lowHpOverheadMessage() {
        return "Low HP!";
    }

    @ConfigItem(keyName = "lowPrayerOverheadMessage", name = "Low Prayer",
            description = "Overhead message to show when Prayer drops to the low Prayer threshold.",
            position = 2, section = alertSection)
    default String lowPrayerOverheadMessage() {
        return "Low Prayer!";
    }

    @Range(min = 1, max = 200)
    @ConfigItem(keyName = "overheadAlertCycles", name = "Duration",
            description = "Client cycles to keep the overhead alert visible.",
            position = 3, section = alertSection)
    default int overheadAlertCycles() {
        return 100;
    }

    @ConfigItem(keyName = "hpNormalColor", name = "HP normal", description = "Color for normal Hitpoints.", position = 0, section = statColorSection)
    default Color hpNormalColor() {
        return new Color(222, 48, 48);
    }

    @ConfigItem(keyName = "hpLowColor", name = "HP low", description = "Color when Hitpoints are low.", position = 1, section = statColorSection)
    default Color hpLowColor() {
        return DEFAULT_DANGER;
    }

    @ConfigItem(keyName = "poisonedHpColor", name = "HP poisoned", description = "Color when poisoned.", position = 2, section = statColorSection)
    default Color poisonedHpColor() {
        return new Color(64, 184, 48);
    }

    @ConfigItem(keyName = "venomedHpColor", name = "HP venomed", description = "Color when venomed.", position = 3, section = statColorSection)
    default Color venomedHpColor() {
        return new Color(18, 122, 30);
    }

    @ConfigItem(keyName = "prayerNormalColor", name = "Prayer normal", description = "Color for normal Prayer.", position = 4, section = statColorSection)
    default Color prayerNormalColor() {
        return new Color(74, 156, 232);
    }

    @ConfigItem(keyName = "prayerLowColor", name = "Prayer low", description = "Color when Prayer is low.", position = 5, section = statColorSection)
    default Color prayerLowColor() {
        return DEFAULT_DANGER;
    }

    @ConfigItem(keyName = "specHighColor", name = "Spec high", description = "Color when special attack is at or above the threshold.", position = 6, section = statColorSection)
    default Color specHighColor() {
        return new Color(255, 212, 64);
    }

    @ConfigItem(keyName = "specLowColor", name = "Spec low", description = "Color when special attack is below the threshold.", position = 7, section = statColorSection)
    default Color specLowColor() {
        return new Color(146, 116, 54);
    }

    @ConfigItem(keyName = "readySpellColor", name = "Ready", description = "Color when a spell is ready.", position = 0, section = spellStateColorSection)
    default Color readySpellColor() {
        return DEFAULT_READY;
    }

    @ConfigItem(keyName = "cooldownSpellColor", name = "Cooldown", description = "Default color when a spell is on cooldown.", position = 1, section = spellStateColorSection)
    default Color cooldownSpellColor() {
        return DEFAULT_COOLDOWN;
    }

    @ConfigItem(keyName = "expiringSpellColor", name = "Expiring", description = "Color when a spell is about to expire.", position = 2, section = spellStateColorSection)
    default Color expiringSpellColor() {
        return DEFAULT_WARNING;
    }

    @ConfigItem(keyName = "thrallActiveColor", name = "Thrall", description = "Active color for Resurrect Thrall.", position = 0, section = activeSpellColorSection)
    default Color thrallActiveColor() {
        return new Color(92, 214, 184);
    }

    @ConfigItem(keyName = "markOfDarknessActiveColor", name = "Mark of Darkness", description = "Active color for Mark of Darkness.", position = 1, section = activeSpellColorSection)
    default Color markOfDarknessActiveColor() {
        return new Color(126, 70, 196);
    }

    @ConfigItem(keyName = "vengeanceActiveColor", name = "Vengeance", description = "Active color for Vengeance.", position = 2, section = activeSpellColorSection)
    default Color vengeanceActiveColor() {
        return new Color(86, 174, 255);
    }

    @ConfigItem(keyName = "corruptionActiveColor", name = "Corruption", description = "Active color for Corruption.", position = 3, section = activeSpellColorSection)
    default Color corruptionActiveColor() {
        return new Color(170, 66, 220);
    }

    @ConfigItem(keyName = "wardOfArceuusActiveColor", name = "Ward of Arceuus", description = "Active color for Ward of Arceuus.", position = 4, section = activeSpellColorSection)
    default Color wardOfArceuusActiveColor() {
        return new Color(64, 196, 210);
    }

    @ConfigItem(keyName = "imbuedHeartActiveColor", name = "Heart", description = "Active color for Imbued or Saturated Heart.", position = 5, section = activeSpellColorSection)
    default Color heartActiveColor() {
        return new Color(214, 62, 156);
    }

    @ConfigItem(keyName = "deathChargeActiveColor", name = "Death Charge", description = "Active color for Death Charge.", position = 6, section = activeSpellColorSection)
    default Color deathChargeActiveColor() {
        return new Color(204, 30, 44);
    }

    @ConfigItem(
            keyName = "deathChargeCooldownColor",
            name = "Death Charge consumed",
            description = "Color after Death Charge restores spec but before the 60-second effect window ends.",
            position = 3,
            section = spellStateColorSection
    )
    default Color deathChargeCooldownColor() {
        return new Color(112, 42, 46);
    }

    @ConfigItem(keyName = "flashReadySpells", name = "Flash ready spells", description = "Flash spells briefly when they become ready.", position = 4, section = spellStateColorSection)
    default boolean flashReadySpells() {
        return false;
    }

    @ConfigItem(keyName = "readySpellFlashColor", name = "Ready flash", description = "Flash color for newly ready spells.", position = 5, section = spellStateColorSection)
    default Color readySpellFlashColor() {
        return new Color(255, 230, 88);
    }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "readySpellFlashRecentSeconds", name = "Flash window", description = "Seconds after becoming ready that a spell may flash. Use 0 to always flash ready spells.", position = 6, section = spellStateColorSection)
    default int readySpellFlashRecentSeconds() {
        return 30;
    }

    @ConfigItem(keyName = "fontType", name = "Font", description = "HUD font family.", position = 0, section = textIconSection)
    default HudFont fontType() {
        return HudFont.SYSTEM;
    }

    @Range(min = 8, max = 32)
    @ConfigItem(keyName = "fontSize", name = "Font size", description = "HUD text size.", position = 1, section = textIconSection)
    default int fontSize() {
        return 16;
    }

    @ConfigItem(keyName = "boldFont", name = "Bold text", description = "Use bold system font text.", position = 2, section = textIconSection)
    default boolean boldFont() {
        return true;
    }

    @ConfigItem(keyName = "verticalLayout", name = "Vertical layout", description = "Use the vertical version of the selected HUD style.", position = 3, section = textIconSection)
    default boolean verticalLayout() {
        return false;
    }

    @Range(min = 10, max = 24)
    @ConfigItem(keyName = "spellIconSize", name = "Spell icon size", description = "Size of spell and cooldown icons in pixels.", position = 4, section = textIconSection)
    default int spellIconSize() {
        return 20;
    }

    @Range(min = 10, max = 24)
    @ConfigItem(keyName = "statIconSize", name = "Stat icon size", description = "Size of Hitpoints, Prayer, and special attack icons in pixels.", position = 5, section = textIconSection)
    default int statIconSize() {
        return 12;
    }

    @Range(min = 0, max = 24)
    @ConfigItem(keyName = "groupGap", name = "Group gap", description = "Spacing between text/icon HUD indicators.", position = 6, section = textIconSection)
    default int groupGap() {
        return 10;
    }

    @Range(min = 0, max = 12)
    @ConfigItem(keyName = "rowGap", name = "Row gap", description = "Spacing between text/icon HUD rows.", position = 7, section = textIconSection)
    default int rowGap() {
        return 2;
    }

    @Range(min = 0, max = 8)
    @ConfigItem(keyName = "iconTextGap", name = "Icon text gap", description = "Spacing between icons and stat text.", position = 8, section = textIconSection)
    default int iconTextGap() {
        return 2;
    }

    @ConfigItem(keyName = "textShadow", name = "Text shadow", description = "Draw a shadow behind HUD text.", position = 9, section = textIconSection)
    default boolean textShadow() {
        return true;
    }

    @ConfigItem(keyName = "shadowColor", name = "Shadow color", description = "Color for text shadows.", position = 10, section = textIconSection)
    default Color shadowColor() {
        return new Color(8, 10, 14, 190);
    }

    @ConfigItem(keyName = "textOutline", name = "Text outline", description = "Draw an outline around HUD text.", position = 11, section = textIconSection)
    default boolean textOutline() {
        return false;
    }

    @ConfigItem(keyName = "outlineColor", name = "Outline color", description = "Color for text outlines.", position = 12, section = textIconSection)
    default Color outlineColor() {
        return new Color(8, 12, 18);
    }

    @Range(min = 100, max = 2000)
    @ConfigItem(keyName = "flashPeriodMillis", name = "Flash speed", description = "Milliseconds per flash phase.", position = 13, section = textIconSection)
    default int flashPeriodMillis() {
        return 500;
    }

    @Range(min = 60, max = 220)
    @ConfigItem(keyName = "barWidth", name = "Width", description = "Horizontal stat bar width.", position = 0, section = barStyleSection)
    default int barWidth() {
        return 112;
    }

    @Range(min = 8, max = 28)
    @ConfigItem(keyName = "barHeight", name = "Height", description = "Horizontal stat bar height.", position = 1, section = barStyleSection)
    default int barHeight() {
        return 14;
    }

    @Range(min = 4, max = 24)
    @ConfigItem(keyName = "verticalBarWidth", name = "Vertical width", description = "Vertical stat meter width.", position = 2, section = barStyleSection)
    default int verticalBarWidth() {
        return 8;
    }

    @Range(min = 30, max = 140)
    @ConfigItem(keyName = "verticalBarHeight", name = "Vertical height", description = "Vertical stat meter height.", position = 3, section = barStyleSection)
    default int verticalBarHeight() {
        return 70;
    }

    @Range(min = 0, max = 12)
    @ConfigItem(keyName = "barGap", name = "Gap", description = "Spacing between bar-style HUD elements.", position = 4, section = barStyleSection)
    default int barGap() {
        return 2;
    }

    @Range(min = 14, max = 40)
    @ConfigItem(keyName = "barSpellTileSize", name = "Spell tile size", description = "Spell tile size for Bars and Orbs styles.", position = 5, section = barStyleSection)
    default int barSpellTileSize() {
        return 22;
    }

    @ConfigItem(keyName = "verticalBarText", name = "Vertical text", description = "Draw stat values vertically inside vertical bars.", position = 6, section = barStyleSection)
    default boolean verticalBarText() {
        return true;
    }

    @ConfigItem(
            keyName = "backgroundColor",
            name = "Color",
            description = "HUD background color.",
            position = 0,
            section = backgroundSection
    )
    default Color backgroundColor() {
        return new Color(12, 16, 22);
    }

    @Range(min = 0, max = 255)
    @ConfigItem(
            keyName = "backgroundAlpha",
            name = "Opacity",
            description = "Background opacity. Use 0 for no background.",
            position = 1,
            section = backgroundSection
    )
    default int backgroundAlpha() {
        return 0;
    }
}
