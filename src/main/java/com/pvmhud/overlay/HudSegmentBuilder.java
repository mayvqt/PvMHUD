package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.GameStateIds;
import com.pvmhud.tracking.HpTracker;
import com.pvmhud.tracking.PrayerTracker;
import com.pvmhud.tracking.SpecTracker;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Singleton
final class HudSegmentBuilder {
    private static final IconRef HITPOINTS_ICON = IconRef.statSkill(Skill.HITPOINTS);
    private static final IconRef PRAYER_ICON = IconRef.statSkill(Skill.PRAYER);
    private static final IconRef SPEC_ICON = IconRef.statSprite(SpriteID.OrbIcon.SPECIAL);
    private static final IconRef HEART_ICON = IconRef.item(ItemID.IMBUED_HEART);

    @Inject
    private Client client;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private HpTracker hpTracker;

    @Inject
    private PrayerTracker prayerTracker;

    @Inject
    private SpecTracker specTracker;

    @Inject
    private HudVisualStateManager stateManager;

    HudFrame build(long now) {
        stateManager.update(now);

        List<Segment> stats = buildStatSegments();
        List<Segment> spells = buildSpellSegments(now);
        List<Segment> hearts = buildHeartSegments(now);

        return new HudFrame(stats, spells, hearts);
    }

    private List<Segment> buildStatSegments() {
        List<Segment> segments = new ArrayList<>(3);

        if (config.showHp()) {
            int hp = hpTracker.getCurrentHp();
            int poison = client.getVarpValue(GameStateIds.POISON);
            segments.add(new Segment(SegmentKind.STAT, "H " + hp, Integer.toString(hp), hpColor(hp, poison), HITPOINTS_ICON));
        }

        if (config.showPrayer()) {
            int prayer = prayerTracker.getCurrentPrayer();
            Color color = prayer <= config.prayerLowThreshold() ? config.prayerLowColor() : config.prayerNormalColor();
            segments.add(new Segment(SegmentKind.STAT, "P " + prayer, Integer.toString(prayer), color, PRAYER_ICON));
        }

        if (config.showSpec()) {
            int spec = specTracker.getSpecPercent();
            Color color = spec >= config.specThreshold() ? config.specHighColor() : config.specLowColor();
            segments.add(new Segment(SegmentKind.STAT, "S " + spec, Integer.toString(spec), color, SPEC_ICON));
        }

        return segments;
    }

    private List<Segment> buildSpellSegments(long now) {
        List<Segment> segments = new ArrayList<>(8);
        List<TrackerDisplay> displays = stateManager.displays();

        for (int i = 0; i < displays.size(); i++) {
            TrackerDisplay display = displays.get(i);
            if (!display.enabled.get()) {
                continue;
            }

            VisualState state = stateManager.spellState(i);
            if (stateManager.shouldRender(state, now, config.showInactiveSpells())) {
                segments.add(new Segment(SegmentKind.SPELL, display.text, "", stateManager.colorFor(display.tracker, state, now), display.icon));
            }
        }

        return segments;
    }

    private List<Segment> buildHeartSegments(long now) {
        List<Segment> segments = new ArrayList<>(1);
        VisualState state = stateManager.heartState();

        if (config.showHeart() && stateManager.shouldRender(state, now, true)) {
            segments.add(new Segment(
                    SegmentKind.HEART,
                    "♥",
                    "",
                    stateManager.colorFor(stateManager.heartTracker(), state, now),
                    HEART_ICON
            ));
        }

        return segments;
    }

    private Color hpColor(int hp, int poisonVarp) {
        if (poisonVarp >= 1_000_000) {
            return config.venomedHpColor();
        }

        if (poisonVarp > 0) {
            return config.poisonedHpColor();
        }

        return hp <= config.hpLowThreshold() ? config.hpLowColor() : config.hpNormalColor();
    }
}
