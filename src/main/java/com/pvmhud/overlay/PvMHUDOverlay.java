package com.pvmhud.overlay;

import com.pvmhud.HudFont;
import com.pvmhud.HudStyle;
import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.GameStateIds;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.HpTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.PrayerTracker;
import com.pvmhud.tracking.SpecTracker;
import com.pvmhud.tracking.SpellStateTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.TimeConstants;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PvMHUDOverlay extends Overlay {
    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 4;
    private static final Skill[] SKILLS = Skill.values();

    private static final IconRef HITPOINTS_ICON = IconRef.statSkill(Skill.HITPOINTS);
    private static final IconRef PRAYER_ICON = IconRef.statSkill(Skill.PRAYER);
    private static final IconRef SPEC_ICON = IconRef.statSprite(SpriteID.OrbIcon.SPECIAL);
    private static final IconRef HEART_ICON = IconRef.item(ItemID.IMBUED_HEART);

    @Inject
    private Client client;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private SkillIconManager skillIconManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private HpTracker hpTracker;

    @Inject
    private PrayerTracker prayerTracker;

    @Inject
    private SpecTracker specTracker;

    @Inject
    private ThrallTracker thrallTracker;

    @Inject
    private VengeanceTracker vengeanceTracker;

    @Inject
    private DeathChargeTracker deathChargeTracker;

    @Inject
    private MarkOfDarknessTracker markOfDarknessTracker;

    @Inject
    private CorruptionTracker corruptionTracker;

    @Inject
    private WardOfArceuusTracker wardOfArceuusTracker;

    @Inject
    private HeartTracker heartTracker;

    private final Map<Long, BufferedImage> iconCache = new HashMap<>();
    private final List<TrackerDisplay> trackerDisplays = new ArrayList<>();
    private final List<VisualState> spellVisualStates = new ArrayList<>();
    private final VisualState heartVisualState = new VisualState();

    private final List<Segment> spellSegments = new ArrayList<>(8);
    private final List<Segment> statSegments = new ArrayList<>(4);
    private final List<Segment> heartSegments = new ArrayList<>(1);
    private final List<Segment> combinedSpellSegments = new ArrayList<>(8);
    private final List<Segment> allSegments = new ArrayList<>(16);

    public PvMHUDOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setDragTargetable(true);
    }

    public void reset() {
        for (VisualState state : spellVisualStates) {
            state.reset();
        }
        heartVisualState.reset();
        iconCache.clear();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (client.getLocalPlayer() == null) {
            return null;
        }

        initialiseDisplays();

        long now = System.nanoTime();
        updateVisualStates(now);

        Font oldFont = graphics.getFont();
        graphics.setFont(resolveFont(oldFont));
        FontMetrics metrics = graphics.getFontMetrics();

        buildSegments(now);

        if (statSegments.isEmpty() && spellSegments.isEmpty() && heartSegments.isEmpty()) {
            graphics.setFont(oldFont);
            return null;
        }

        Dimension dimension;
        switch (config.hudStyle()) {
            case GAME_ICONS:
                dimension = renderTextOrIconHud(graphics, metrics, true);
                break;
            case BARS:
                dimension = renderBars(graphics, metrics);
                break;
            case CHIPS:
                dimension = renderChips(graphics, metrics);
                break;
            case ORBS:
                dimension = renderOrbs(graphics, metrics);
                break;
            case STACK:
                dimension = renderStack(graphics, metrics);
                break;
            case TEXT:
            default:
                dimension = renderTextOrIconHud(graphics, metrics, false);
                break;
        }

        graphics.setFont(oldFont);
        return dimension;
    }

    private void initialiseDisplays() {
        if (!trackerDisplays.isEmpty()) {
            return;
        }

        trackerDisplays.add(new TrackerDisplay(thrallTracker, "T", IconRef.spell(SpriteID.MagicNecroOn.RESURRECT_SUPERIOR_SKELETON), config::showThrall));
        trackerDisplays.add(new TrackerDisplay(deathChargeTracker, "D", IconRef.spell(SpriteID.MagicNecroOn.DEATH_CHARGE), config::showDeathCharge));
        trackerDisplays.add(new TrackerDisplay(markOfDarknessTracker, "M", IconRef.spell(SpriteID.MagicNecroOn.MARK_OF_DARKNESS), config::showMarkOfDarkness));
        trackerDisplays.add(new TrackerDisplay(vengeanceTracker, "V", IconRef.spell(SpriteID.LunarMagicOn.VENGEANCE), config::showVengeance));
        trackerDisplays.add(new TrackerDisplay(corruptionTracker, "C", IconRef.spell(SpriteID.MagicNecroOn.GREATER_CORRUPTION), config::showCorruption));
        trackerDisplays.add(new TrackerDisplay(wardOfArceuusTracker, "W", IconRef.spell(SpriteID.MagicNecroOn.WARD_OF_ARCEUUS), config::showWardOfArceuus));

        for (int i = 0; i < trackerDisplays.size(); i++) {
            spellVisualStates.add(new VisualState());
        }
    }

    private void updateVisualStates(long now) {
        for (int i = 0; i < trackerDisplays.size(); i++) {
            updateVisualState(trackerDisplays.get(i).tracker, spellVisualStates.get(i), now);
        }
        updateVisualState(heartTracker, heartVisualState, now);
    }

    private void updateVisualState(SpellStateTracker tracker, VisualState state, long now) {
        boolean active = tracker.isActive();
boolean cooldown = !active && tracker.isOnCooldown();
boolean ready = !active && !cooldown;
        boolean expiringSoon = tracker.isExpiringSoon(config.spellExpiringSoonSeconds());

        if (state.initialised) {
            if (state.ready && !ready) {
                state.lastTransitionNanos = now;
            } else if (!state.ready && ready) {
                state.lastTransitionNanos = now;
            }
        } else {
            state.initialised = true;
        }

        if (!ready) {
            state.lastVisibleNanos = now;
        }

        state.active = active;
        state.cooldown = cooldown;
        state.ready = ready;
        state.expiringSoon = expiringSoon;

        if (ready && state.lastTransitionNanos > 0L && config.readySpellFlashRecentSeconds() > 0) {
            long flashWindowNanos = TimeConstants.secondsToNanos(config.readySpellFlashRecentSeconds());
            if (now - state.lastTransitionNanos > flashWindowNanos) {
                state.lastTransitionNanos = 0L;
            }
        }
    }

    private void buildSegments(long now) {
        statSegments.clear();
        spellSegments.clear();
        heartSegments.clear();

        buildStatSegments();

        for (int i = 0; i < trackerDisplays.size(); i++) {
            TrackerDisplay display = trackerDisplays.get(i);
            if (!display.enabled.get()) {
                continue;
            }

            VisualState state = spellVisualStates.get(i);
            if (shouldRender(state, now, config.showInactiveSpells())) {
                spellSegments.add(new Segment(SegmentKind.SPELL, display.text, "", colorFor(display.tracker, state, now), display.icon));
            }
        }

        if (config.showHeart() && shouldRender(heartVisualState, now, true)) {
            heartSegments.add(new Segment(SegmentKind.HEART, "♥", "", colorFor(heartTracker, heartVisualState, now), HEART_ICON));
        }
    }

    private void buildStatSegments() {
        if (config.showHp()) {
            int hp = hpTracker.getCurrentHp();
            int poison = client.getVarpValue(GameStateIds.POISON);
            Color color = hpColor(hp, poison);
            statSegments.add(new Segment(SegmentKind.STAT, "H " + hp, Integer.toString(hp), color, HITPOINTS_ICON));
        }

        if (config.showPrayer()) {
            int prayer = prayerTracker.getCurrentPrayer();
            Color color = prayer <= config.prayerLowThreshold() ? config.prayerLowColor() : config.prayerNormalColor();
            statSegments.add(new Segment(SegmentKind.STAT, "P " + prayer, Integer.toString(prayer), color, PRAYER_ICON));
        }

        if (config.showSpec()) {
            int spec = specTracker.getSpecPercent();
            Color color = spec >= config.specThreshold() ? config.specHighColor() : config.specLowColor();
            statSegments.add(new Segment(SegmentKind.STAT, "S " + spec, Integer.toString(spec), color, SPEC_ICON));
        }
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

    private boolean shouldRender(VisualState state, long now, boolean allowInactive) {
        if (!state.ready) {
            return true;
        }

        return allowInactive
                && config.inactiveSpellTimeoutSeconds() > 0
                && state.lastVisibleNanos > 0L
                && now - state.lastVisibleNanos <= TimeConstants.secondsToNanos(config.inactiveSpellTimeoutSeconds());
    }

    private Color colorFor(SpellStateTracker tracker, VisualState state, long now) {
        if (state.expiringSoon) {
            return config.expiringSpellColor();
        }

        if (state.active) {
            return activeColor(tracker);
        }

        if (state.cooldown) {
            return tracker == deathChargeTracker ? config.deathChargeCooldownColor() : config.cooldownSpellColor();
        }

        if (shouldFlashReady(state, now)) {
            return config.readySpellFlashColor();
        }

        return config.readySpellColor();
    }

    private boolean shouldFlashReady(VisualState state, long now) {
        if (!config.flashReadySpells()) {
            return false;
        }

        int windowSeconds = config.readySpellFlashRecentSeconds();
        if (windowSeconds == 0) {
            return isFlashPhase(now);
        }

        return state.lastTransitionNanos > 0L
                && now - state.lastTransitionNanos <= TimeConstants.secondsToNanos(windowSeconds)
                && isFlashPhase(now);
    }

    private boolean isFlashPhase(long now) {
        long period = Math.max(100L, config.flashPeriodMillis()) * TimeConstants.NS_PER_MS;
        return (now / period) % 2L == 0L;
    }

    private Color activeColor(SpellStateTracker tracker) {
        if (tracker == thrallTracker) {
            return config.thrallActiveColor();
        }
        if (tracker == markOfDarknessTracker) {
            return config.markOfDarknessActiveColor();
        }
        if (tracker == deathChargeTracker) {
            return config.deathChargeActiveColor();
        }
        if (tracker == vengeanceTracker) {
            return config.vengeanceActiveColor();
        }
        if (tracker == corruptionTracker) {
            return config.corruptionActiveColor();
        }
        if (tracker == wardOfArceuusTracker) {
            return config.wardOfArceuusActiveColor();
        }
        if (tracker == heartTracker) {
            return config.heartActiveColor();
        }

        return config.readySpellColor();
    }

    private Dimension renderTextOrIconHud(Graphics2D graphics, FontMetrics metrics, boolean iconsOnly) {
        List<List<Segment>> rows = new ArrayList<>(3);
        if (!spellSegments.isEmpty()) {
            rows.add(spellSegments);
        }
        if (!statSegments.isEmpty()) {
            rows.add(statSegments);
        }
        if (!heartSegments.isEmpty()) {
            rows.add(heartSegments);
        }

        if (rows.isEmpty()) {
            return null;
        }

        if (config.verticalLayout()) {
            List<Segment> all = allSegments();
            int width = maxSegmentWidth(metrics, all, iconsOnly) + PADDING_X * 2;
            int rowHeight = rowHeight(metrics, all);
            int height = all.size() * rowHeight + Math.max(0, all.size() - 1) * rowGap() + PADDING_Y * 2;
            drawBackground(graphics, width, height);

            int y = PADDING_Y;
            for (Segment segment : all) {
                drawSegment(graphics, metrics, segment, PADDING_X, y, rowHeight, iconsOnly);
                y += rowHeight + rowGap();
            }
            return new Dimension(width, height);
        }

        int width = 0;
        int height = PADDING_Y * 2;
        for (List<Segment> row : rows) {
            width = Math.max(width, rowWidth(metrics, row, iconsOnly));
            height += rowHeight(metrics, row);
        }
        height += Math.max(0, rows.size() - 1) * rowGap();
        width += PADDING_X * 2;

        drawBackground(graphics, width, height);

        int y = PADDING_Y;
        for (List<Segment> row : rows) {
            int rowHeight = rowHeight(metrics, row);
            int rowWidth = rowWidth(metrics, row, iconsOnly);
            int x = PADDING_X + (width - PADDING_X * 2 - rowWidth) / 2;
            for (Segment segment : row) {
                int segmentWidth = segmentWidth(metrics, segment, iconsOnly);
                drawSegment(graphics, metrics, segment, x, y, rowHeight, iconsOnly);
                x += segmentWidth + groupGap();
            }
            y += rowHeight + rowGap();
        }

        return new Dimension(width, height);
    }

    private Dimension renderChips(Graphics2D graphics, FontMetrics metrics) {
        List<Segment> spells = combinedSpellSegments();
        int gap = Math.max(1, groupGap() / 2);
        int chipHeight = chipHeight(metrics);

        if (config.verticalLayout()) {
            List<Segment> all = allSegments();
            int width = maxChipWidth(metrics, all) + PADDING_X * 2;
            int height = all.size() * chipHeight + Math.max(0, all.size() - 1) * gap + PADDING_Y * 2;
            drawBackground(graphics, width, height);

            int y = PADDING_Y;
            for (Segment segment : all) {
                drawChip(graphics, metrics, segment, PADDING_X, y, width - PADDING_X * 2, chipHeight);
                y += chipHeight + gap;
            }
            return new Dimension(width, height);
        }

        int statWidth = chipsWidth(metrics, statSegments, gap);
        int spellWidth = chipsWidth(metrics, spells, gap);
        int width = Math.max(statWidth, spellWidth) + PADDING_X * 2;
        int rows = (statSegments.isEmpty() ? 0 : 1) + (spells.isEmpty() ? 0 : 1);
        int height = rows * chipHeight + Math.max(0, rows - 1) * rowGap() + PADDING_Y * 2;

        drawBackground(graphics, width, height);

        int y = PADDING_Y;
        if (!statSegments.isEmpty()) {
            drawChipRow(graphics, metrics, statSegments, y, width, statWidth, gap, chipHeight);
            y += chipHeight + rowGap();
        }
        if (!spells.isEmpty()) {
            drawChipRow(graphics, metrics, spells, y, width, spellWidth, gap, chipHeight);
        }

        return new Dimension(width, height);
    }

    private Dimension renderBars(Graphics2D graphics, FontMetrics metrics) {
        List<Segment> spells = combinedSpellSegments();
        int gap = barGap();
        int tile = barSpellTileSize();
        int spellWidth = spells.isEmpty() ? 0 : spells.size() * tile + (spells.size() - 1) * gap;

        if (config.verticalLayout()) {
            int barWidth = Math.max(1, config.verticalBarWidth());
            int statColumnWidth = statSegments.isEmpty() ? 0 : statSegments.size() * Math.max(config.statIconSize(), barWidth) + Math.max(0, statSegments.size() - 1) * gap;
            int spellHeight = spells.isEmpty() ? 0 : spells.size() * tile + Math.max(0, spells.size() - 1) * gap;
            int statHeight = statSegments.isEmpty() ? 0 : config.statIconSize() + iconTextGap() + config.verticalBarHeight();
            int columnGap = !spells.isEmpty() && !statSegments.isEmpty() ? gap + 2 : 0;

            int width = (spells.isEmpty() ? 0 : tile) + columnGap + statColumnWidth + PADDING_X * 2;
            int height = Math.max(spellHeight, statHeight) + PADDING_Y * 2;
            drawBackground(graphics, width, height);

            int spellY = PADDING_Y + Math.max(0, (height - PADDING_Y * 2 - spellHeight) / 2);
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, PADDING_X, spellY, tile);
                spellY += tile + gap;
            }

            int x = PADDING_X + (spells.isEmpty() ? 0 : tile + columnGap);
            int y = PADDING_Y + Math.max(0, (height - PADDING_Y * 2 - statHeight) / 2);
            for (Segment segment : statSegments) {
                drawVerticalStatBar(graphics, metrics, segment, x, y, Math.max(config.statIconSize(), barWidth), statHeight);
                x += Math.max(config.statIconSize(), barWidth) + gap;
            }

            return new Dimension(width, height);
        }

        int statWidth = statSegments.isEmpty() ? 0 : config.statIconSize() + iconTextGap() + config.barWidth();
        int width = Math.max(spellWidth, statWidth) + PADDING_X * 2;
        int rowHeight = horizontalStatRowHeight();
        int height = PADDING_Y * 2
                + (spells.isEmpty() ? 0 : tile)
                + (!spells.isEmpty() && !statSegments.isEmpty() ? gap + 1 : 0)
                + (statSegments.isEmpty() ? 0 : statSegments.size() * rowHeight + Math.max(0, statSegments.size() - 1) * gap);

        drawBackground(graphics, width, height);

        int y = PADDING_Y;
        if (!spells.isEmpty()) {
            int x = PADDING_X + (width - PADDING_X * 2 - spellWidth) / 2;
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, x, y, tile);
                x += tile + gap;
            }
            y += tile + gap + 1;
        }

        int statX = PADDING_X + (width - PADDING_X * 2 - statWidth) / 2;
        for (Segment segment : statSegments) {
            drawHorizontalStatBar(graphics, metrics, segment, statX, y, rowHeight);
            y += rowHeight + gap;
        }

        return new Dimension(width, height);
    }

    private Dimension renderOrbs(Graphics2D graphics, FontMetrics metrics) {
        List<Segment> spells = combinedSpellSegments();
        int gap = barGap();
        int orb = orbSize();
        int tile = barSpellTileSize();

        if (config.verticalLayout()) {
            int statHeight = statSegments.isEmpty() ? 0 : statSegments.size() * orb + Math.max(0, statSegments.size() - 1) * gap;
            int spellHeight = spells.isEmpty() ? 0 : spells.size() * tile + Math.max(0, spells.size() - 1) * gap;
            int columnGap = !statSegments.isEmpty() && !spells.isEmpty() ? gap + 2 : 0;
            int width = (statSegments.isEmpty() ? 0 : orb) + columnGap + (spells.isEmpty() ? 0 : tile) + PADDING_X * 2;
            int height = Math.max(statHeight, spellHeight) + PADDING_Y * 2;
            drawBackground(graphics, width, height);

            int statY = PADDING_Y + Math.max(0, (height - PADDING_Y * 2 - statHeight) / 2);
            for (Segment segment : statSegments) {
                drawStatOrb(graphics, metrics, segment, PADDING_X, statY, orb);
                statY += orb + gap;
            }

            int spellX = PADDING_X + (statSegments.isEmpty() ? 0 : orb + columnGap);
            int spellY = PADDING_Y + Math.max(0, (height - PADDING_Y * 2 - spellHeight) / 2);
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, spellX, spellY, tile);
                spellY += tile + gap;
            }

            return new Dimension(width, height);
        }

        int statWidth = statSegments.isEmpty() ? 0 : statSegments.size() * orb + Math.max(0, statSegments.size() - 1) * gap;
        int spellWidth = spells.isEmpty() ? 0 : spells.size() * tile + Math.max(0, spells.size() - 1) * gap;
        int width = Math.max(statWidth, spellWidth) + PADDING_X * 2;
        int rows = (statSegments.isEmpty() ? 0 : 1) + (spells.isEmpty() ? 0 : 1);
        int height = (statSegments.isEmpty() ? 0 : orb) + (spells.isEmpty() ? 0 : tile)
                + Math.max(0, rows - 1) * gap + PADDING_Y * 2;
        drawBackground(graphics, width, height);

        int y = PADDING_Y;
        if (!statSegments.isEmpty()) {
            int x = PADDING_X + (width - PADDING_X * 2 - statWidth) / 2;
            for (Segment segment : statSegments) {
                drawStatOrb(graphics, metrics, segment, x, y, orb);
                x += orb + gap;
            }
            y += orb + gap;
        }

        if (!spells.isEmpty()) {
            int x = PADDING_X + (width - PADDING_X * 2 - spellWidth) / 2;
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, x, y, tile);
                x += tile + gap;
            }
        }

        return new Dimension(width, height);
    }

    private Dimension renderStack(Graphics2D graphics, FontMetrics metrics) {
        List<Segment> all = allSegments();
        int iconSize = Math.max(config.statIconSize(), config.spellIconSize());
        int rowHeight = Math.max(iconSize, metrics.getHeight());
        int gap = rowGap();

        int width = 0;
        for (Segment segment : all) {
            width = Math.max(width, iconSize + iconTextGap() + 6 + metrics.stringWidth(label(segment)));
        }
        width += PADDING_X * 2;

        int height = all.size() * rowHeight + Math.max(0, all.size() - 1) * gap + PADDING_Y * 2;
        drawBackground(graphics, width, height);

        int y = PADDING_Y;
        for (Segment segment : all) {
            BufferedImage icon = loadIcon(segment.icon, iconSize);
            int iconY = y + (rowHeight - iconSize) / 2;
            if (icon != null) {
                graphics.drawImage(icon, PADDING_X, iconY, null);
            } else {
                drawText(graphics, label(segment).substring(0, 1), PADDING_X, y + baseline(metrics, rowHeight), segment.color, metrics);
            }

            int textX = PADDING_X + iconSize + iconTextGap() + 4;
            graphics.setColor(segment.color);
            graphics.fillRect(PADDING_X + iconSize + Math.max(1, iconTextGap() / 2), y + 2, 2, rowHeight - 4);
            drawText(graphics, label(segment), textX, y + baseline(metrics, rowHeight), segment.color, metrics);

            y += rowHeight + gap;
        }

        return new Dimension(width, height);
    }

    private void drawSegment(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int rowHeight, boolean iconsOnly) {
        if (iconsOnly && segment.icon != null) {
            int iconSize = iconSize(segment);
            BufferedImage icon = loadIcon(segment.icon, iconSize);
            if (icon != null) {
                graphics.drawImage(icon, x, y + (rowHeight - iconSize) / 2, null);
                return;
            }
        }

        drawText(graphics, segment.text, x, y + baseline(metrics, rowHeight), segment.color, metrics);
    }

    private void drawChipRow(Graphics2D graphics, FontMetrics metrics, List<Segment> segments, int y, int width, int rowWidth, int gap, int chipHeight) {
        int x = PADDING_X + Math.max(0, (width - PADDING_X * 2 - rowWidth) / 2);
        for (Segment segment : segments) {
            int chipWidth = chipWidth(metrics, segment);
            drawChip(graphics, metrics, segment, x, y, chipWidth, chipHeight);
            x += chipWidth + gap;
        }
    }

   private void drawChip(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int width, int height) {
    int alpha = config.backgroundAlpha();

    if (alpha > 0) {
        graphics.setColor(withAlpha(config.backgroundColor(), alpha));
        graphics.fillRoundRect(x, y, width, height, 7, 7);
    }

    graphics.setColor(withAlpha(segment.color, Math.max(90, alpha)));
    graphics.drawRoundRect(x, y, width - 1, height - 1, 7, 7);

    if (segment.kind == SegmentKind.STAT) {
        graphics.fillRoundRect(x + 1, y + 1, 3, height - 2, 4, 4);
    }

    int iconSize = iconSize(segment);

    int iconX;
    if (segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART) {
        iconX = x + (width - iconSize) / 2;
    } else {
        iconX = x + 7;
    }

    int iconY = y + (height - iconSize) / 2;

    BufferedImage icon = loadIcon(segment.icon, iconSize);
    if (icon != null) {
        graphics.drawImage(icon, iconX, iconY, null);
    }

    String text = label(segment);
    if (!text.isEmpty()) {
        drawText(
                graphics,
                text,
                iconX + iconSize + iconTextGap(),
                y + baseline(metrics, height),
                segment.color,
                metrics
        );
    }
}

    private void drawSpellTile(Graphics2D graphics, Segment segment, int x, int y, int size) {
        int alpha = config.backgroundAlpha();
        graphics.setColor(withAlpha(segment.color, Math.max(55, alpha / 2)));
        graphics.fillRoundRect(x, y, size, size, 6, 6);
        graphics.setColor(withAlpha(segment.color, 220));
        graphics.drawRoundRect(x, y, size - 1, size - 1, 6, 6);

        BufferedImage icon = loadIcon(segment.icon, Math.max(10, size - 6));
        if (icon != null) {
            int iconX = x + (size - icon.getWidth()) / 2;
            int iconY = y + (size - icon.getHeight()) / 2;
            graphics.drawImage(icon, iconX, iconY, null);
        }
    }

    private void drawHorizontalStatBar(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int height) {
        int iconSize = config.statIconSize();
        BufferedImage icon = loadIcon(segment.icon, iconSize);
        if (icon != null) {
            graphics.drawImage(icon, x, y + (height - iconSize) / 2, null);
        }

        int barX = x + iconSize + iconTextGap();
        int barY = y + Math.max(0, (height - config.barHeight()) / 2);
        int barWidth = config.barWidth();
        int barHeight = config.barHeight();

        graphics.setColor(withAlpha(config.backgroundColor(), Math.max(40, config.backgroundAlpha())));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        graphics.setColor(withAlpha(segment.color, 180));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        graphics.setColor(Color.WHITE);
        drawCenteredText(graphics, metrics, label(segment), barX, barY, barWidth, barHeight);
    }

    private void drawVerticalStatBar(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int width, int height) {
        int iconSize = config.statIconSize();
        BufferedImage icon = loadIcon(segment.icon, iconSize);
        if (icon != null) {
            graphics.drawImage(icon, x + (width - iconSize) / 2, y, null);
        }

        int barY = y + iconSize + iconTextGap();
        int barHeight = config.verticalBarHeight();
        int barWidth = Math.max(1, config.verticalBarWidth());
        int barX = x + (width - barWidth) / 2;

        graphics.setColor(withAlpha(config.backgroundColor(), Math.max(40, config.backgroundAlpha())));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        graphics.setColor(withAlpha(segment.color, 180));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);

        if (config.verticalBarText()) {
            graphics.setColor(Color.WHITE);
            drawCenteredText(graphics, metrics, label(segment), x, barY, width, barHeight);
        }
    }

    private void drawStatOrb(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int size) {
        graphics.setColor(withAlpha(segment.color, Math.max(45, config.backgroundAlpha() / 2)));
        graphics.fillOval(x + 3, y + 3, Math.max(1, size - 6), Math.max(1, size - 6));
        graphics.setColor(withAlpha(segment.color, 220));
        graphics.drawOval(x, y, size - 1, size - 1);
        graphics.setColor(Color.WHITE);
        drawCenteredText(graphics, metrics, label(segment), x, y, size, size);
    }

    private void drawText(Graphics2D graphics, String text, int x, int y, Color color, FontMetrics metrics) {
        if (text == null || text.isEmpty()) {
            return;
        }

        if (config.textOutline()) {
            graphics.setColor(config.outlineColor());
            graphics.drawString(text, x - 1, y);
            graphics.drawString(text, x + 1, y);
            graphics.drawString(text, x, y - 1);
            graphics.drawString(text, x, y + 1);
        } else if (config.textShadow()) {
            graphics.setColor(config.shadowColor());
            graphics.drawString(text, x + 1, y + 1);
        }

        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D graphics, FontMetrics metrics, String text, int x, int y, int width, int height) {
        int textWidth = metrics.stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + baseline(metrics, height);
        graphics.drawString(text, textX, textY);
    }

    private void drawBackground(Graphics2D graphics, int width, int height) {
        int alpha = config.backgroundAlpha();
        if (alpha <= 0) {
            return;
        }

        graphics.setColor(withAlpha(config.backgroundColor(), alpha));
        graphics.fillRoundRect(0, 0, width, height, 8, 8);
    }

    private BufferedImage loadIcon(IconRef iconRef, int size) {
        if (iconRef == null || size <= 0) {
            return null;
        }

        long key = iconRef.cacheKey(size);
        BufferedImage cached = iconCache.get(key);
        if (cached != null) {
            return cached;
        }

        BufferedImage image = null;
        switch (iconRef.group) {
            case STAT:
                image = skillIconManager.getSkillImage(SKILLS[iconRef.id]);
                break;
            case SPRITE:
            case SPELL:
                try {
                    image = spriteManager.getSprite(iconRef.id, 0);
                } catch (Exception ignored) {
                    image = null;
                }
                break;
            case ITEM:
                image = itemManager.getImage(iconRef.id);
                break;
            default:
                break;
        }

        if (image == null) {
            return null;
        }

        BufferedImage scaled = ImageUtil.resizeImage(image, size, size);
        iconCache.put(key, scaled);
        return scaled;
    }

    private Font resolveFont(Font baseFont) {
        HudFont font = config.fontType();
        switch (font) {
            case RUNESCAPE:
                return FontManager.getRunescapeFont().deriveFont((float) config.fontSize());
            case RUNESCAPE_BOLD:
                return FontManager.getRunescapeBoldFont().deriveFont((float) config.fontSize());
            case RUNESCAPE_SMALL:
                return FontManager.getRunescapeSmallFont().deriveFont((float) config.fontSize());
            case SYSTEM:
            default:
                return baseFont.deriveFont(config.boldFont() ? Font.BOLD : Font.PLAIN, (float) config.fontSize());
        }
    }

    private List<Segment> combinedSpellSegments() {
        combinedSpellSegments.clear();
        combinedSpellSegments.addAll(spellSegments);
        combinedSpellSegments.addAll(heartSegments);
        return combinedSpellSegments;
    }

    private List<Segment> allSegments() {
        allSegments.clear();
        allSegments.addAll(statSegments);
        allSegments.addAll(spellSegments);
        allSegments.addAll(heartSegments);
        return allSegments;
    }

    private int rowWidth(FontMetrics metrics, List<Segment> segments, boolean iconsOnly) {
        if (segments.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (Segment segment : segments) {
            width += segmentWidth(metrics, segment, iconsOnly) + groupGap();
        }
        return width - groupGap();
    }

    private int rowHeight(FontMetrics metrics, List<Segment> segments) {
        int height = metrics.getHeight();
        for (Segment segment : segments) {
            height = Math.max(height, iconSize(segment));
        }
        return height;
    }

    private int maxSegmentWidth(FontMetrics metrics, List<Segment> segments, boolean iconsOnly) {
        int width = 0;
        for (Segment segment : segments) {
            width = Math.max(width, segmentWidth(metrics, segment, iconsOnly));
        }
        return width;
    }

    private int segmentWidth(FontMetrics metrics, Segment segment, boolean iconsOnly) {
        if (iconsOnly && segment.icon != null) {
            return iconSize(segment);
        }
        return metrics.stringWidth(segment.text);
    }

    private int chipsWidth(FontMetrics metrics, List<Segment> segments, int gap) {
        if (segments.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (Segment segment : segments) {
            width += chipWidth(metrics, segment) + gap;
        }
        return width - gap;
    }

    private int maxChipWidth(FontMetrics metrics, List<Segment> segments) {
        int width = 0;
        for (Segment segment : segments) {
            width = Math.max(width, chipWidth(metrics, segment));
        }
        return width;
    }

 private int chipWidth(FontMetrics metrics, Segment segment) {
    if (segment.kind == SegmentKind.STAT) {
        return config.statChipWidth();
    }

    // Spells (and heart) = icon only, no text spacing
    if (segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART) {
        return iconSize(segment) + 10;
    }

    // Everything else keeps text
    return iconSize(segment)
            + iconTextGap()
            + metrics.stringWidth(label(segment))
            + 14;
}

    private int chipHeight(FontMetrics metrics) {
        return Math.max(metrics.getHeight(), Math.max(config.statIconSize(), config.spellIconSize())) + 6;
    }

    private int horizontalStatRowHeight() {
        return Math.max(config.statIconSize(), config.barHeight());
    }

    private int orbSize() {
        return Math.max(26, Math.max(config.statIconSize() + 14, config.fontSize() + 12));
    }

    private int barSpellTileSize() {
        return Math.max(14, config.barSpellTileSize());
    }

    private int iconSize(Segment segment) {
        return segment.kind == SegmentKind.STAT ? config.statIconSize() : config.spellIconSize();
    }

    private int groupGap() {
        return Math.max(0, config.groupGap());
    }

    private int rowGap() {
        return Math.max(0, config.rowGap());
    }

    private int barGap() {
        return Math.max(0, config.barGap());
    }

    private int iconTextGap() {
        return Math.max(0, config.iconTextGap());
    }

    private int baseline(FontMetrics metrics, int height) {
        return ((height - metrics.getHeight()) / 2) + metrics.getAscent();
    }

    private String label(Segment segment) {
    if (segment.kind == SegmentKind.SPELL || segment.kind == SegmentKind.HEART) {
        return "";
    }

    return segment.iconText.isEmpty() ? segment.text : segment.iconText;
}

    private static Color withAlpha(Color color, int alpha) {
        Color safeColor = color == null ? Color.BLACK : color;
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        return new Color(safeColor.getRed(), safeColor.getGreen(), safeColor.getBlue(), safeAlpha);
    }

    private interface BooleanSupplier {
        boolean get();
    }

    private static final class TrackerDisplay {
        private final SpellStateTracker tracker;
        private final String text;
        private final IconRef icon;
        private final BooleanSupplier enabled;

        private TrackerDisplay(SpellStateTracker tracker, String text, IconRef icon, BooleanSupplier enabled) {
            this.tracker = tracker;
            this.text = text;
            this.icon = icon;
            this.enabled = enabled;
        }
    }

    private static final class VisualState {
        private boolean initialised;
        private boolean active;
        private boolean cooldown;
        private boolean ready = true;
        private boolean expiringSoon;
        private long lastVisibleNanos;
        private long lastTransitionNanos;

        private void reset() {
            initialised = false;
            active = false;
            cooldown = false;
            ready = true;
            expiringSoon = false;
            lastVisibleNanos = 0L;
            lastTransitionNanos = 0L;
        }
    }

    private enum SegmentKind {
        STAT,
        SPELL,
        HEART
    }

    private enum IconGroup {
        STAT,
        SPRITE,
        SPELL,
        ITEM
    }

    private static final class IconRef {
        private final IconGroup group;
        private final int id;

        private IconRef(IconGroup group, int id) {
            this.group = group;
            this.id = id;
        }

        private static IconRef statSkill(Skill skill) {
            return new IconRef(IconGroup.STAT, skill.ordinal());
        }

        private static IconRef statSprite(int spriteId) {
            return new IconRef(IconGroup.SPRITE, spriteId);
        }

        private static IconRef spell(int spriteId) {
            return new IconRef(IconGroup.SPELL, spriteId);
        }

        private static IconRef item(int itemId) {
            return new IconRef(IconGroup.ITEM, itemId);
        }

        private long cacheKey(int size) {
            return ((long) group.ordinal() << 48) | ((long) id << 16) | size;
        }
    }

    private static final class Segment {
        private final SegmentKind kind;
        private final String text;
        private final String iconText;
        private final Color color;
        private final IconRef icon;

        private Segment(SegmentKind kind, String text, String iconText, Color color, IconRef icon) {
            this.kind = kind;
            this.text = text == null ? "" : text;
            this.iconText = iconText == null ? "" : iconText;
            this.color = color == null ? Color.WHITE : color;
            this.icon = icon;
        }
    }
}
