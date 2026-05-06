package com.pvmhud;

import com.pvmhud.overlay.HudFont;
import com.pvmhud.overlay.HudStyle;
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

    @ConfigSection(name = "HUD", description = "Choose the HUD style and layout.", position = 0)
    String generalSection = "general";

    @ConfigSection(name = "Tracked Indicators", description = "Choose which stats, spells, and cooldowns are shown.", position = 1)
    String indicatorSection = "indicators";

    @ConfigSection(name = "Text & Icons", description = "Configure Text and Game Icons layouts.", position = 2)
    String textIconSection = "textIcon";

    @ConfigSection(name = "Bars, Chips & Orbs", description = "Configure Bars, Chips, Orbs, and Stack layouts.", position = 3)
    String barStyleSection = "barStyle";

    @ConfigSection(name = "Alerts", description = "Configure local-only overhead alerts.", position = 4)
    String alertSection = "alerts";

    @ConfigSection(name = "Thresholds", description = "Configure HP, Prayer, and Special Attack thresholds.", position = 5)
    String thresholdSection = "thresholds";

    @ConfigSection(name = "Spell Timing", description = "Configure spell visibility, expiry warnings, and ready flashes.", position = 6)
    String timingSection = "timing";

    @ConfigSection(name = "Background", description = "Configure the HUD background.", position = 7)
    String backgroundSection = "background";

    @ConfigSection(name = "Stat Colors", description = "Set colors for HP, Prayer, Special Attack, poison, and venom.", position = 8)
    String statColorSection = "statColors";

    @ConfigSection(name = "Spell State Colors", description = "Set shared colors for ready, cooldown, expiring, and flash states.", position = 9)
    String spellStateColorSection = "spellStateColors";

    @ConfigSection(name = "Active Spell Colors", description = "Set active colors for each tracked spell or cooldown.", position = 10)
    String activeSpellColorSection = "activeSpellColors";

    @ConfigItem(keyName = "hudStyle", name = "HUD Style", description = "Choose how PvM HUD is displayed.", position = 0, section = generalSection)
    default HudStyle hudStyle() { return HudStyle.CHIPS; }

    @ConfigItem(keyName = "showThrall", name = "Thrall", description = "Show Resurrect Thrall timer and state.", position = 3, section = indicatorSection)
    default boolean showThrall() { return true; }

    @ConfigItem(keyName = "showVengeance", name = "Vengeance", description = "Show Vengeance active/cooldown state.", position = 4, section = indicatorSection)
    default boolean showVengeance() { return true; }

    @ConfigItem(keyName = "showDeathCharge", name = "Death Charge", description = "Show Death Charge active/cooldown state.", position = 5, section = indicatorSection)
    default boolean showDeathCharge() { return true; }

    @ConfigItem(keyName = "showMarkOfDarkness", name = "Mark of Darkness", description = "Show Mark of Darkness active/expiring state.", position = 6, section = indicatorSection)
    default boolean showMarkOfDarkness() { return true; }

    @ConfigItem(keyName = "showCorruption", name = "Corruption", description = "Show Corruption cooldown state.", position = 7, section = indicatorSection)
    default boolean showCorruption() { return true; }

    @ConfigItem(keyName = "showWardOfArceuus", name = "Ward of Arceuus", description = "Show Ward of Arceuus active/cooldown state.", position = 8, section = indicatorSection)
    default boolean showWardOfArceuus() { return true; }

    @ConfigItem(keyName = "showImbuedHeart", name = "Heart", description = "Show Imbued/Saturated Heart cooldown state.", position = 9, section = indicatorSection)
    default boolean showHeart() { return true; }

    @ConfigItem(keyName = "showHp", name = "Hitpoints", description = "Show boosted Hitpoints.", position = 0, section = indicatorSection)
    default boolean showHp() { return true; }

    @ConfigItem(keyName = "showPrayer", name = "Prayer", description = "Show boosted Prayer.", position = 1, section = indicatorSection)
    default boolean showPrayer() { return true; }

    @ConfigItem(keyName = "showSpec", name = "Special Attack", description = "Show Special Attack energy.", position = 2, section = indicatorSection)
    default boolean showSpec() { return true; }

    @ConfigItem(keyName = "showInactiveSpells", name = "Show Recently-Ready Spells", description = "Keep spell indicators visible briefly after they become ready.", position = 0, section = timingSection)
    default boolean showInactiveSpells() { return true; }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "inactiveSpellTimeoutSeconds", name = "Ready Visibility Time", description = "Seconds to keep a ready spell visible after its active/cooldown state ends.", position = 1, section = timingSection)
    default int inactiveSpellTimeoutSeconds() { return 10; }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "spellExpiringSoonSeconds", name = "Expiry Warning Time", description = "Seconds before a spell expires to switch to the expiry warning color.", position = 2, section = timingSection)
    default int spellExpiringSoonSeconds() { return 5; }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "hpLowThreshold", name = "Low HP Threshold", description = "HP value at or below which low-HP colors and alerts are used.", position = 0, section = thresholdSection)
    default int hpLowThreshold() { return 45; }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "prayerLowThreshold", name = "Low Prayer Threshold", description = "Prayer value at or below which low-Prayer colors and alerts are used.", position = 1, section = thresholdSection)
    default int prayerLowThreshold() { return 10; }

    @Range(min = 0, max = 100)
    @ConfigItem(keyName = "specThreshold", name = "Spec Threshold", description = "Special Attack percentage at or above which high-spec colors and alerts are used.", position = 2, section = thresholdSection)
    default int specThreshold() { return 50; }

    @ConfigItem(keyName = "overheadHpAlertEnabled", name = "Low HP Alert", description = "Show a local overhead message when HP crosses below the low-HP threshold.", position = 0, section = alertSection)
    default boolean overheadHpAlertEnabled() { return true; }

    @ConfigItem(keyName = "lowHpOverheadMessage", name = "Low HP Message", description = "Overhead message shown when HP crosses below the low-HP threshold.", position = 1, section = alertSection)
    default String lowHpOverheadMessage() { return "Low HP!"; }

    @ConfigItem(keyName = "overheadPrayerAlertEnabled", name = "Low Prayer Alert", description = "Show a local overhead message when Prayer crosses below the low-Prayer threshold.", position = 2, section = alertSection)
    default boolean overheadPrayerAlertEnabled() { return true; }

    @ConfigItem(keyName = "lowPrayerOverheadMessage", name = "Low Prayer Message", description = "Overhead message shown when Prayer crosses below the low-Prayer threshold.", position = 3, section = alertSection)
    default String lowPrayerOverheadMessage() { return "Low Prayer!"; }

    @ConfigItem(keyName = "overheadSpecAlertEnabled", name = "Spec Alert", description = "Show a local overhead message when Special Attack crosses above the spec threshold.", position = 4, section = alertSection)
    default boolean overheadSpecAlertEnabled() { return false; }

    @ConfigItem(keyName = "specOverheadMessage", name = "Spec Message", description = "Overhead message shown when Special Attack crosses above the spec threshold.", position = 5, section = alertSection)
    default String specOverheadMessage() { return "Spec!"; }

    @Range(min = 1, max = 200)
    @ConfigItem(keyName = "overheadAlertCycles", name = "Alert Duration", description = "How long overhead alerts stay visible, measured in client cycles.", position = 6, section = alertSection)
    default int overheadAlertCycles() { return 200; }

    @ConfigItem(keyName = "hpNormalColor", name = "HP", description = "Color for normal HP.", position = 0, section = statColorSection)
    default Color hpNormalColor() { return new Color(222, 48, 48); }

    @ConfigItem(keyName = "hpLowColor", name = "Low HP", description = "Color for low HP.", position = 1, section = statColorSection)
    default Color hpLowColor() { return DEFAULT_DANGER; }

    @ConfigItem(keyName = "poisonedHpColor", name = "Poisoned HP", description = "Color for HP while poisoned.", position = 2, section = statColorSection)
    default Color poisonedHpColor() { return new Color(64, 184, 48); }

    @ConfigItem(keyName = "venomedHpColor", name = "Venomed HP", description = "Color for HP while venomed.", position = 3, section = statColorSection)
    default Color venomedHpColor() { return new Color(18, 122, 30); }

    @ConfigItem(keyName = "prayerNormalColor", name = "Prayer", description = "Color for normal Prayer.", position = 4, section = statColorSection)
    default Color prayerNormalColor() { return new Color(74, 156, 232); }

    @ConfigItem(keyName = "prayerLowColor", name = "Low Prayer", description = "Color for low Prayer.", position = 5, section = statColorSection)
    default Color prayerLowColor() { return DEFAULT_DANGER; }

    @ConfigItem(keyName = "specHighColor", name = "High Spec", description = "Color for Special Attack at or above the spec threshold.", position = 6, section = statColorSection)
    default Color specHighColor() { return new Color(255, 212, 64); }

    @ConfigItem(keyName = "specLowColor", name = "Low Spec", description = "Color for Special Attack below the spec threshold.", position = 7, section = statColorSection)
    default Color specLowColor() { return new Color(146, 116, 54); }

    @ConfigItem(keyName = "readySpellColor", name = "Ready", description = "Color for ready spells.", position = 0, section = spellStateColorSection)
    default Color readySpellColor() { return DEFAULT_READY; }

    @ConfigItem(keyName = "cooldownSpellColor", name = "Cooldown", description = "Color for spells on cooldown.", position = 1, section = spellStateColorSection)
    default Color cooldownSpellColor() { return DEFAULT_COOLDOWN; }

    @ConfigItem(keyName = "expiringSpellColor", name = "Expiring Soon", description = "Color for spells close to expiring.", position = 2, section = spellStateColorSection)
    default Color expiringSpellColor() { return DEFAULT_WARNING; }

    @ConfigItem(keyName = "deathChargeCooldownColor", name = "Death Charge Consumed", description = "Color for Death Charge after spec restore but before the effect fully ends.", position = 3, section = spellStateColorSection)
    default Color deathChargeCooldownColor() { return new Color(112, 42, 46); }

    @ConfigItem(keyName = "flashReadySpells", name = "Flash When Ready", description = "Flash spell indicators when they become ready.", position = 3, section = timingSection)
    default boolean flashReadySpells() { return true; }

    @ConfigItem(keyName = "readySpellFlashColor", name = "Ready Flash", description = "Flash color for newly-ready spells.", position = 4, section = spellStateColorSection)
    default Color readySpellFlashColor() { return new Color(255, 230, 88); }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "readySpellFlashRecentSeconds", name = "Ready Flash Window", description = "Seconds after becoming ready that a spell can flash. Set to 0 to always flash ready spells.", position = 4, section = timingSection)
    default int readySpellFlashRecentSeconds() { return 30; }

    @ConfigItem(keyName = "thrallActiveColor", name = "Thrall", description = "Active color for Thrall.", position = 0, section = activeSpellColorSection)
    default Color thrallActiveColor() { return new Color(92, 214, 184); }

    @ConfigItem(keyName = "markOfDarknessActiveColor", name = "Mark of Darkness", description = "Active color for Mark of Darkness.", position = 1, section = activeSpellColorSection)
    default Color markOfDarknessActiveColor() { return new Color(126, 70, 196); }

    @ConfigItem(keyName = "vengeanceActiveColor", name = "Vengeance", description = "Active color for Vengeance.", position = 2, section = activeSpellColorSection)
    default Color vengeanceActiveColor() { return new Color(86, 174, 255); }

    @ConfigItem(keyName = "corruptionActiveColor", name = "Corruption", description = "Active color for Corruption.", position = 3, section = activeSpellColorSection)
    default Color corruptionActiveColor() { return new Color(170, 66, 220); }

    @ConfigItem(keyName = "wardOfArceuusActiveColor", name = "Ward of Arceuus", description = "Active color for Ward of Arceuus.", position = 4, section = activeSpellColorSection)
    default Color wardOfArceuusActiveColor() { return new Color(64, 196, 210); }

    @ConfigItem(keyName = "imbuedHeartActiveColor", name = "Heart", description = "Active color for Imbued/Saturated Heart.", position = 5, section = activeSpellColorSection)
    default Color heartActiveColor() { return new Color(214, 62, 156); }

    @ConfigItem(keyName = "deathChargeActiveColor", name = "Death Charge", description = "Active color for Death Charge.", position = 6, section = activeSpellColorSection)
    default Color deathChargeActiveColor() { return new Color(204, 30, 44); }

    @ConfigItem(keyName = "fontType", name = "Font", description = "Font used by Text and Game Icons layouts.", position = 0, section = textIconSection)
    default HudFont fontType() { return HudFont.SYSTEM; }

    @Range(min = 8, max = 32)
    @ConfigItem(keyName = "fontSize", name = "Font Size", description = "Text size used by Text and Game Icons layouts.", position = 1, section = textIconSection)
    default int fontSize() { return 16; }

    @ConfigItem(keyName = "boldFont", name = "Bold Font", description = "Use bold text when using the system font.", position = 2, section = textIconSection)
    default boolean boldFont() { return true; }

    @ConfigItem(keyName = "verticalLayout", name = "Vertical Layout", description = "Stack the selected HUD style vertically instead of horizontally.", position = 1, section = generalSection)
    default boolean verticalLayout() { return true; }

    @Range(min = 10, max = 32)
    @ConfigItem(keyName = "spellIconSize", name = "Spell Icon Size", description = "Size of spell and cooldown icons.", position = 3, section = textIconSection)
    default int spellIconSize() { return 20; }

    @Range(min = 10, max = 32)
    @ConfigItem(keyName = "statIconSize", name = "Stat Icon Size", description = "Size of HP, Prayer, and Special Attack icons.", position = 4, section = textIconSection)
    default int statIconSize() { return 14; }

    @Range(min = 0, max = 24)
    @ConfigItem(keyName = "groupGap", name = "Item Spacing", description = "Spacing between HUD indicators.", position = 5, section = textIconSection)
    default int groupGap() { return 7; }

    @Range(min = 0, max = 12)
    @ConfigItem(keyName = "rowGap", name = "Row Spacing", description = "Spacing between HUD rows.", position = 6, section = textIconSection)
    default int rowGap() { return 0; }

    @Range(min = 0, max = 8)
    @ConfigItem(keyName = "iconTextGap", name = "Icon/Value Spacing", description = "Spacing between an icon and its value text.", position = 7, section = textIconSection)
    default int iconTextGap() { return 2; }

    @ConfigItem(keyName = "textShadow", name = "Text Shadow", description = "Draw a shadow behind HUD text.", position = 8, section = textIconSection)
    default boolean textShadow() { return true; }

    @ConfigItem(keyName = "shadowColor", name = "Shadow Color", description = "Color used for text shadows.", position = 9, section = textIconSection)
    default Color shadowColor() { return new Color(8, 10, 14, 190); }

    @ConfigItem(keyName = "textOutline", name = "Text Outline", description = "Draw an outline around HUD text.", position = 10, section = textIconSection)
    default boolean textOutline() { return true; }

    @ConfigItem(keyName = "outlineColor", name = "Outline Color", description = "Color used for text outlines.", position = 11, section = textIconSection)
    default Color outlineColor() { return new Color(8, 12, 18); }

    @Range(min = 100, max = 2000)
    @ConfigItem(keyName = "flashPeriodMillis", name = "Flash Speed", description = "Milliseconds per flash phase. Lower values flash faster.", position = 5, section = timingSection)
    default int flashPeriodMillis() { return 500; }

    @Range(min = 60, max = 220)
    @ConfigItem(keyName = "barWidth", name = "Bar Width", description = "Width of horizontal stat bars.", position = 0, section = barStyleSection)
    default int barWidth() { return 112; }

    @Range(min = 8, max = 28)
    @ConfigItem(keyName = "barHeight", name = "Bar Height", description = "Height of horizontal stat bars.", position = 1, section = barStyleSection)
    default int barHeight() { return 14; }

    @Range(min = 4, max = 24)
    @ConfigItem(keyName = "verticalBarWidth", name = "Vertical Bar Width", description = "Width of vertical stat bars.", position = 2, section = barStyleSection)
    default int verticalBarWidth() { return 20; }

    @Range(min = 30, max = 140)
    @ConfigItem(keyName = "verticalBarHeight", name = "Vertical Bar Height", description = "Height of vertical stat bars.", position = 3, section = barStyleSection)
    default int verticalBarHeight() { return 70; }

    @Range(min = 0, max = 12)
    @ConfigItem(keyName = "barGap", name = "Bar Spacing", description = "Spacing between Bars, Orbs, and spell tiles.", position = 4, section = barStyleSection)
    default int barGap() { return 1; }

    @Range(min = 14, max = 40)
    @ConfigItem(keyName = "barSpellTileSize", name = "Spell Tile Size", description = "Size of spell tiles in Bars and Orbs layouts.", position = 5, section = barStyleSection)
    default int barSpellTileSize() { return 22; }

    @ConfigItem(keyName = "verticalBarText", name = "Show Vertical Values", description = "Draw stat values inside vertical bars.", position = 6, section = barStyleSection)
    default boolean verticalBarText() { return true; }

    @Range(min = 28, max = 96)
    @ConfigItem(keyName = "statChipWidth", name = "Stat Chip Width", description = "Fixed width for HP, Prayer, and Special Attack chips.", position = 7, section = barStyleSection)
    default int statChipWidth() { return 48; }

    @ConfigItem(keyName = "backgroundColor", name = "Background Color", description = "HUD background color.", position = 0, section = backgroundSection)
    default Color backgroundColor() { return new Color(12, 16, 22); }

    @Range(min = 0, max = 255)
    @ConfigItem(keyName = "backgroundAlpha", name = "Background Opacity", description = "HUD background opacity. Set to 0 for no background.", position = 1, section = backgroundSection)
    default int backgroundAlpha() { return 0; }
}
