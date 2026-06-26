package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CombatBoost;
import com.pvmhud.tracking.CombatBoosts;
import com.pvmhud.tracking.PotionDisplayText;
import com.pvmhud.tracking.PotionBoostTracker;
import com.pvmhud.tracking.TimedPotionEffect;
import com.pvmhud.tracking.TimedPotionTracker;
import com.pvmhud.tracking.TimedPotionType;
import com.pvmhud.tracking.TimeConstants;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Singleton
final class PotionSegmentBuilder {
    @Inject
    private PvMHUDConfig config;

    @Inject
    private PotionBoostTracker boostTracker;

    @Inject
    private TimedPotionTracker timedPotionTracker;

    HudFrame buildTimers(long now) {
        if (!config.showPotionOverlay()) {
            return new HudFrame(List.of(), List.of(), List.of());
        }

        List<Segment> timerSegments = buildTimerSegments(now);
        return new HudFrame(List.of(), timerSegments, List.of());
    }

    HudFrame buildBoosts(long now) {
        if (!config.showCombatBoosts()) {
            return new HudFrame(List.of(), List.of(), List.of());
        }

        List<Segment> boostSegments = buildBoostSegments(now);
        return new HudFrame(boostSegments, List.of(), List.of());
    }

    private List<Segment> buildBoostSegments(long now) {
        List<Segment> segments = new ArrayList<>(CombatBoosts.TRACKED_SKILLS.length);

        for (int i = 0; i < CombatBoosts.TRACKED_SKILLS.length; i++) {
            CombatBoost boost = boostTracker.boostAt(i);
            if (!boost.isBoosted() || !CombatBoosts.isEnabled(config, boost.getSkill())) {
                continue;
            }

            Color color = boostColor(boost, now);
            segments.add(new Segment(
                    SegmentKind.STAT,
                    PotionDisplayText.boostLabel(boost),
                    PotionDisplayText.boostIconText(boost),
                    color,
                    IconRef.statSkill(boost.getSkill())
            ));
        }

        return segments;
    }

    private List<Segment> buildTimerSegments(long now) {
        List<Segment> segments = new ArrayList<>(TimedPotionType.values().length);

        if (config.showRaidPotionTimers()) {
            timedPotionTracker.forEachRaidPotion(effect -> addTimerSegment(segments, effect, now));
        }

        if (config.showDivinePotionTimers()) {
            timedPotionTracker.forEachDivinePotion(effect -> addTimerSegment(segments, effect, now));
        }

        return segments;
    }

    private void addTimerSegment(List<Segment> segments, TimedPotionEffect effect, long now) {
        if (!effect.isActive()) {
            return;
        }

        TimedPotionType type = effect.getType();
        Color color = effect.isExpiringSoon(config.potionExpiringSoonSeconds())
                ? flashingWarningColor(now)
                : config.potionActiveColor();

        segments.add(new Segment(
                SegmentKind.POTION,
                timerLabel(effect),
                PotionDisplayText.timedPotionIconText(effect),
                color,
                IconRef.item(type.getItemId())
        ));
    }

    private Color boostColor(CombatBoost boost, long now) {
        if (boost.isAtOrBelowPercentThreshold(config.combatBoostThresholdPercent())) {
            return flashingWarningColor(now);
        }

        double progress = Math.max(0.0d, Math.min(1.0d, boost.getRemainingBoostPercent() / 100.0d));
        return blend(config.potionWarningColor(), config.potionActiveColor(), progress);
    }

    private Color flashingWarningColor(long now) {
        long period = Math.max(100L, config.flashPeriodMillis()) * TimeConstants.NS_PER_MS;
        return (now / period) % 2L == 0L ? config.potionFlashColor() : config.potionWarningColor();
    }

    private static Color blend(Color start, Color end, double progress) {
        double clamped = Math.max(0.0d, Math.min(1.0d, progress));
        int red = (int) Math.round(start.getRed() + (end.getRed() - start.getRed()) * clamped);
        int green = (int) Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * clamped);
        int blue = (int) Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * clamped);
        int alpha = (int) Math.round(start.getAlpha() + (end.getAlpha() - start.getAlpha()) * clamped);
        return new Color(red, green, blue, alpha);
    }

    private String timerLabel(TimedPotionEffect effect) {
        return PotionDisplayText.timedPotionLabel(effect);
    }

}
