package com.pvmhud;

import com.pvmhud.overlay.HudFont;
import com.pvmhud.overlay.HudStyle;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

import java.awt.Color;

@ConfigGroup(PvMHUDConfig.GROUP)
public interface PvMHUDConfig extends Config {
    String GROUP = "pvmhud";

    Color DEFAULT_READY = new Color(224, 206, 156);
    Color DEFAULT_COOLDOWN = new Color(118, 108, 92);
    Color DEFAULT_DANGER = new Color(132, 28, 28);
    Color DEFAULT_WARNING = new Color(245, 170, 58);

    @ConfigSection(name = "Main HUD", description = "Choose the HUD style and layout.", position = 0)
    String generalSection = "general";
    @ConfigSection(name = "Indicators", description = "Choose which stats, spells, and cooldowns are shown.", position = 1)
    String indicatorSection = "indicators";
    @ConfigSection(name = "Alerts", description = "Configure local overhead alerts.", position = 3)
    String alertSection = "alerts";
    @ConfigSection(name = "Thresholds", description = "Configure stat thresholds.", position = 4)
    String thresholdSection = "thresholds";
    @ConfigSection(name = "Spell Timing", description = "Configure spell visibility and ready-state timing.", position = 5)
    String timingSection = "timing";

    @ConfigSection(name = "Text & Icon Layout", description = "Fine-tune text, icon, and spacing settings.", position = 6, closedByDefault = true)
    String textIconSection = "textIcon";

    @ConfigSection(name = "Bars & Chips Layout", description = "Fine-tune sizing for Bars and Chips layouts.", position = 7, closedByDefault = true)
    String barStyleSection = "barStyle";

    @ConfigSection(name = "Background", description = "Configure HUD background color and opacity.", position = 8, closedByDefault = true)
    String backgroundSection = "background";

    @ConfigSection(name = "Stat Colors", description = "Set colors for HP, Prayer, Special Attack, poison, and venom states.", position = 9, closedByDefault = true)
    String statColorSection = "statColors";

    @ConfigSection(name = "Spell State Colors", description = "Set shared colors for ready, cooldown, expiring, consumed, and flash states.", position = 10, closedByDefault = true)
    String spellStateColorSection = "spellStateColors";

    @ConfigSection(name = "Active Spell Colors", description = "Set active colors for each tracked spell and cooldown.", position = 11, closedByDefault = true)
    String activeSpellColorSection = "activeSpellColors";

    @ConfigItem(keyName = "hudStyle", name = "Style", description = "Choose the layout used by the main PvM HUD.", position = 0, section = generalSection)
    default HudStyle hudStyle() { return HudStyle.CHIPS; }

    @ConfigItem(keyName = "showThrall", name = "Thrall", description = "Show Resurrect Thrall active time, cooldown, and ready state.", position = 3, section = indicatorSection)
    default boolean showThrall() { return true; }

    @ConfigItem(keyName = "showVengeance", name = "Vengeance", description = "Show Vengeance active, cooldown, and ready state.", position = 4, section = indicatorSection)
    default boolean showVengeance() { return true; }

    @ConfigItem(keyName = "showDeathCharge", name = "Death Charge", description = "Show Death Charge active, consumed, cooldown, and ready state.", position = 5, section = indicatorSection)
    default boolean showDeathCharge() { return true; }

    @ConfigItem(keyName = "showMarkOfDarkness", name = "Mark of Darkness", description = "Show Mark of Darkness active, expiring, faded, and ready state.", position = 6, section = indicatorSection)
    default boolean showMarkOfDarkness() { return true; }

    @ConfigItem(keyName = "showCorruption", name = "Corruption", description = "Show Corruption cooldown and ready state.", position = 7, section = indicatorSection)
    default boolean showCorruption() { return true; }

    @ConfigItem(keyName = "showWardOfArceuus", name = "Ward of Arceuus", description = "Show Ward of Arceuus active, cooldown, and ready state.", position = 8, section = indicatorSection)
    default boolean showWardOfArceuus() { return true; }

    @ConfigItem(keyName = "showImbuedHeart", name = "Heart", description = "Show Imbued Heart and Saturated Heart cooldown state.", position = 9, section = indicatorSection)
    default boolean showHeart() { return true; }

    @ConfigItem(keyName = "showHp", name = "Hitpoints", description = "Show current boosted Hitpoints, including poison and venom colors.", position = 0, section = indicatorSection)
    default boolean showHp() { return true; }

    @ConfigItem(keyName = "showPrayer", name = "Prayer", description = "Show current Prayer points.", position = 1, section = indicatorSection)
    default boolean showPrayer() { return true; }

    @ConfigItem(keyName = "showSpec", name = "Special Attack", description = "Show current Special Attack energy.", position = 2, section = indicatorSection)
    default boolean showSpec() { return true; }

    @ConfigItem(keyName = "showInactiveSpells", name = "Show Ready Spells", description = "Keep spell indicators visible briefly after their active or cooldown state ends.", position = 0, section = timingSection)
    default boolean showInactiveSpells() { return true; }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "inactiveSpellTimeoutSeconds", name = "Ready Visibility Time", description = "Seconds to keep a ready spell visible after it becomes inactive. Set to 0 to hide ready spells immediately.", position = 1, section = timingSection)
    default int inactiveSpellTimeoutSeconds() { return 10; }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "spellExpiringSoonSeconds", name = "Spell Warning Time", description = "Seconds before an active spell expires to switch to the expiring color.", position = 2, section = timingSection)
    default int spellExpiringSoonSeconds() { return 5; }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "hpLowThreshold", name = "Low HP", description = "Hitpoints value at or below which low-HP colors and alerts are used.", position = 2, section = thresholdSection)
    default int hpLowThreshold() { return 45; }

    @Range(min = 0, max = 99)
    @ConfigItem(keyName = "prayerLowThreshold", name = "Low Prayer", description = "Prayer value at or below which low-Prayer colors and alerts are used.", position = 3, section = thresholdSection)
    default int prayerLowThreshold() { return 10; }

    @Range(min = 0, max = 100)
    @ConfigItem(keyName = "specThreshold", name = "Spec Ready", description = "Special Attack percentage at or above which high-spec colors and alerts are used.", position = 4, section = thresholdSection)
    default int specThreshold() { return 50; }

    @ConfigItem(keyName = "overheadHpAlertEnabled", name = "Low HP Alert", description = "Show a local overhead message when Hitpoints crosses below the low-HP threshold.", position = 0, section = alertSection)
    default boolean overheadHpAlertEnabled() { return true; }

    @ConfigItem(keyName = "lowHpOverheadMessage", name = "Low HP Message", description = "Overhead message shown when Hitpoints crosses below the low-HP threshold.", position = 1, section = alertSection)
    default String lowHpOverheadMessage() { return "Low HP!"; }

    @ConfigItem(keyName = "overheadPrayerAlertEnabled", name = "Low Prayer Alert", description = "Show a local overhead message when Prayer crosses below the low-Prayer threshold.", position = 2, section = alertSection)
    default boolean overheadPrayerAlertEnabled() { return true; }

    @ConfigItem(keyName = "lowPrayerOverheadMessage", name = "Low Prayer Message", description = "Overhead message shown when Prayer crosses below the low-Prayer threshold.", position = 3, section = alertSection)
    default String lowPrayerOverheadMessage() { return "Low Prayer!"; }

    @ConfigItem(keyName = "overheadSpecAlertEnabled", name = "Spec Ready Alert", description = "Show a local overhead message when Special Attack crosses above the spec-ready threshold.", position = 4, section = alertSection)
    default boolean overheadSpecAlertEnabled() { return false; }

    @ConfigItem(keyName = "specOverheadMessage", name = "Spec Ready Message", description = "Overhead message shown when Special Attack crosses above the spec-ready threshold.", position = 5, section = alertSection)
    default String specOverheadMessage() { return "Spec!"; }

    @Range(min = 1, max = 10)
    @ConfigItem(keyName = "overheadAlertSeconds", name = "Alert Duration", description = "Seconds that local overhead alert messages stay visible.", position = 6, section = alertSection)
    default int overheadAlertSeconds() { return 4; }

    @ConfigItem(keyName = "hpNormalColor", name = "Hitpoints", description = "Color for normal Hitpoints in all HUD styles.", position = 0, section = statColorSection)
    default Color hpNormalColor() { return new Color(224, 64, 64); }

    @ConfigItem(keyName = "hpLowColor", name = "Low Hitpoints", description = "Color for Hitpoints at or below the low-HP threshold.", position = 1, section = statColorSection)
    default Color hpLowColor() { return DEFAULT_DANGER; }

    @ConfigItem(keyName = "poisonedHpColor", name = "Poisoned Hitpoints", description = "Color for Hitpoints while poisoned.", position = 2, section = statColorSection)
    default Color poisonedHpColor() { return new Color(86, 208, 72); }

    @ConfigItem(keyName = "venomedHpColor", name = "Venomed Hitpoints", description = "Color for Hitpoints while venomed.", position = 3, section = statColorSection)
    default Color venomedHpColor() { return new Color(24, 132, 44); }

    @ConfigItem(keyName = "prayerNormalColor", name = "Prayer", description = "Color for normal Prayer in all HUD styles.", position = 4, section = statColorSection)
    default Color prayerNormalColor() { return new Color(84, 168, 244); }

    @ConfigItem(keyName = "prayerLowColor", name = "Low Prayer", description = "Color for Prayer at or below the low-Prayer threshold.", position = 5, section = statColorSection)
    default Color prayerLowColor() { return new Color(34, 72, 130); }

    @ConfigItem(keyName = "specHighColor", name = "Spec Ready", description = "Color for Special Attack at or above the spec-ready threshold.", position = 6, section = statColorSection)
    default Color specHighColor() { return new Color(255, 214, 78); }

    @ConfigItem(keyName = "specLowColor", name = "Spec Building", description = "Color for Special Attack below the spec-ready threshold.", position = 7, section = statColorSection)
    default Color specLowColor() { return new Color(162, 126, 62); }

    @ConfigItem(keyName = "readySpellColor", name = "Ready", description = "Shared color for ready spells in all HUD styles.", position = 0, section = spellStateColorSection)
    default Color readySpellColor() { return DEFAULT_READY; }

    @ConfigItem(keyName = "cooldownSpellColor", name = "Cooldown", description = "Shared color for spells on cooldown in all HUD styles.", position = 1, section = spellStateColorSection)
    default Color cooldownSpellColor() { return DEFAULT_COOLDOWN; }

    @ConfigItem(keyName = "expiringSpellColor", name = "Expiring Soon", description = "Shared color for spells close to expiring in all HUD styles.", position = 2, section = spellStateColorSection)
    default Color expiringSpellColor() { return DEFAULT_WARNING; }

    @ConfigItem(keyName = "deathChargeCooldownColor", name = "Death Charge Consumed", description = "Color for Death Charge after the spec restore is consumed but before the effect fully ends.", position = 3, section = spellStateColorSection)
    default Color deathChargeCooldownColor() { return new Color(132, 48, 58); }

    @ConfigItem(keyName = "flashReadySpells", name = "Flash Ready Spells", description = "Flash spell indicators when they become ready.", position = 3, section = timingSection)
    default boolean flashReadySpells() { return true; }

    @ConfigItem(keyName = "readySpellFlashColor", name = "Ready Flash", description = "Flash color for newly ready spells.", position = 4, section = spellStateColorSection)
    default Color readySpellFlashColor() { return new Color(255, 232, 98); }

    @Range(min = 0, max = 600)
    @ConfigItem(keyName = "readySpellFlashRecentSeconds", name = "Ready Flash Window", description = "Seconds after becoming ready that a spell can flash. Set to 0 to allow ready spells to keep flashing.", position = 4, section = timingSection)
    default int readySpellFlashRecentSeconds() { return 30; }

    @ConfigItem(keyName = "thrallActiveColor", name = "Thrall", description = "Active color for Thrall in all HUD styles.", position = 0, section = activeSpellColorSection)
    default Color thrallActiveColor() { return new Color(104, 220, 192); }

    @ConfigItem(keyName = "markOfDarknessActiveColor", name = "Mark of Darkness", description = "Active color for Mark of Darkness in all HUD styles.", position = 1, section = activeSpellColorSection)
    default Color markOfDarknessActiveColor() { return new Color(136, 84, 210); }

    @ConfigItem(keyName = "vengeanceActiveColor", name = "Vengeance", description = "Active color for Vengeance in all HUD styles.", position = 2, section = activeSpellColorSection)
    default Color vengeanceActiveColor() { return new Color(98, 184, 255); }

    @ConfigItem(keyName = "corruptionActiveColor", name = "Corruption", description = "Active color for Corruption in all HUD styles.", position = 3, section = activeSpellColorSection)
    default Color corruptionActiveColor() { return new Color(182, 78, 234); }

    @ConfigItem(keyName = "wardOfArceuusActiveColor", name = "Ward of Arceuus", description = "Active color for Ward of Arceuus in all HUD styles.", position = 4, section = activeSpellColorSection)
    default Color wardOfArceuusActiveColor() { return new Color(76, 210, 224); }

    @ConfigItem(keyName = "imbuedHeartActiveColor", name = "Heart", description = "Active color for Imbued/Saturated Heart in all HUD styles.", position = 5, section = activeSpellColorSection)
    default Color heartActiveColor() { return new Color(222, 74, 166); }

    @ConfigItem(keyName = "deathChargeActiveColor", name = "Death Charge", description = "Active color for Death Charge in all HUD styles.", position = 6, section = activeSpellColorSection)
    default Color deathChargeActiveColor() { return new Color(214, 40, 56); }

    @ConfigItem(keyName = "fontType", name = "Font", description = "Font used by every HUD style with text.", position = 0, section = textIconSection)
    default HudFont fontType() { return HudFont.SYSTEM; }

    @Range(min = 8, max = 32)
    @ConfigItem(keyName = "fontSize", name = "Font Size", description = "Text size used by every HUD style with text.", position = 1, section = textIconSection)
    default int fontSize() { return 16; }

    @ConfigItem(keyName = "boldFont", name = "Bold Font", description = "Use bold text when using the system font (all HUD styles with text).", position = 2, section = textIconSection)
    default boolean boldFont() { return true; }

    @ConfigItem(keyName = "verticalLayout", name = "Vertical Layout", description = "Stack Text, Game Icons, Bars, and Chips vertically instead of horizontally.", position = 1, section = generalSection)
    default boolean verticalLayout() { return true; }

    @Range(min = 10, max = 32)
    @ConfigItem(keyName = "spellIconSize", name = "Spell Icon Size", description = "Size of spell and cooldown icons in Game Icons, Chips, and Player Bound layouts.", position = 3, section = textIconSection)
    default int spellIconSize() { return 20; }

    @Range(min = 10, max = 32)
    @ConfigItem(keyName = "statIconSize", name = "Stat Icon Size", description = "Size of HP, Prayer, and Special Attack icons in Game Icons and Chips layouts.", position = 4, section = textIconSection)
    default int statIconSize() { return 14; }

    @Range(min = 0, max = 24)
    @ConfigItem(keyName = "groupGap", name = "Item Spacing", description = "Spacing between indicators in Text, Game Icons, and Chips layouts.", position = 5, section = textIconSection)
    default int groupGap() { return 7; }

    @Range(min = 0, max = 12)
    @ConfigItem(keyName = "rowGap", name = "Row Spacing", description = "Spacing between rows in Text, Game Icons, and Chips layouts.", position = 6, section = textIconSection)
    default int rowGap() { return 0; }

    @Range(min = 0, max = 8)
    @ConfigItem(keyName = "iconTextGap", name = "Icon/Value Spacing", description = "Spacing between icon and value text in Game Icons, Bars, and Chips layouts.", position = 7, section = textIconSection)
    default int iconTextGap() { return 2; }

    @ConfigItem(keyName = "textShadow", name = "Text Shadow", description = "Draw a shadow behind text in all HUD styles with text.", position = 8, section = textIconSection)
    default boolean textShadow() { return true; }

    @ConfigItem(keyName = "shadowColor", name = "Shadow Color", description = "Color used for text shadows in all HUD styles with text.", position = 9, section = textIconSection)
    default Color shadowColor() { return new Color(8, 10, 14, 190); }

    @ConfigItem(keyName = "textOutline", name = "Text Outline", description = "Draw an outline around text in all HUD styles with text.", position = 10, section = textIconSection)
    default boolean textOutline() { return true; }

    @ConfigItem(keyName = "outlineColor", name = "Outline Color", description = "Color used for text outlines in all HUD styles with text.", position = 11, section = textIconSection)
    default Color outlineColor() { return new Color(8, 12, 18); }

    @Range(min = 100, max = 2000)
    @ConfigItem(keyName = "flashPeriodMillis", name = "Flash Speed", description = "Milliseconds per flash phase. Lower values flash faster.", position = 5, section = timingSection)
    default int flashPeriodMillis() { return 500; }

    @Range(min = 60, max = 220)
    @ConfigItem(keyName = "barWidth", name = "Bar Width", description = "Width of horizontal stat bars in Bars layout.", position = 0, section = barStyleSection)
    default int barWidth() { return 112; }

    @Range(min = 8, max = 28)
    @ConfigItem(keyName = "barHeight", name = "Bar Height", description = "Height of horizontal stat bars in Bars layout.", position = 1, section = barStyleSection)
    default int barHeight() { return 14; }

    @Range(min = 4, max = 24)
    @ConfigItem(keyName = "verticalBarWidth", name = "Vertical Bar Width", description = "Width of vertical stat bars in Bars layout when Vertical Layout is enabled.", position = 2, section = barStyleSection)
    default int verticalBarWidth() { return 20; }

    @Range(min = 30, max = 140)
    @ConfigItem(keyName = "verticalBarHeight", name = "Vertical Bar Height", description = "Height of vertical stat bars in Bars layout when Vertical Layout is enabled.", position = 3, section = barStyleSection)
    default int verticalBarHeight() { return 70; }

    @Range(min = 0, max = 12)
    @ConfigItem(keyName = "barGap", name = "Bar Spacing", description = "Spacing in Bars layout.", position = 4, section = barStyleSection)
    default int barGap() { return 1; }

    @Range(min = 14, max = 40)
    @ConfigItem(keyName = "barSpellTileSize", name = "Spell Tile Size", description = "Size of spell tiles in Bars layout.", position = 5, section = barStyleSection)
    default int barSpellTileSize() { return 22; }

    @ConfigItem(keyName = "verticalBarText", name = "Show Vertical Values", description = "Draw stat values inside Bars layout vertical bars.", position = 6, section = barStyleSection)
    default boolean verticalBarText() { return true; }

    @Range(min = 28, max = 96)
    @ConfigItem(keyName = "statChipWidth", name = "Stat Chip Width", description = "Fixed width for HP, Prayer, and Special Attack chips in Chips layout.", position = 7, section = barStyleSection)
    default int statChipWidth() { return 48; }

    @ConfigItem(keyName = "backgroundColor", name = "Background Color", description = "HUD background color for all HUD styles.", position = 0, section = backgroundSection)
    default Color backgroundColor() { return new Color(12, 16, 22); }

    @Range(min = 0, max = 255)
    @ConfigItem(keyName = "backgroundAlpha", name = "Background Opacity", description = "HUD background opacity for all HUD styles. Set to 0 for no background.", position = 1, section = backgroundSection)
    default int backgroundAlpha() { return 0; }
}
