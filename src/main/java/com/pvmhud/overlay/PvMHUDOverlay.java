package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.HudStyle;
import com.pvmhud.tracking.*;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.ui.FontManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
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
    private static final IconRef HEART_ICON = IconRef.spellItem(ItemID.IMBUED_HEART);
    private final List<Segment> spellSegments = new ArrayList<>(8);
    private final List<Segment> statSegments = new ArrayList<>(4);
    private final List<Segment> heartSegments = new ArrayList<>(2);
    private final List<Segment> compactSpellSegments = new ArrayList<>(8);
    private final List<Segment> allSegments = new ArrayList<>(12);
    private final IndicatorState heartState = new IndicatorState();
    private final Map<Long, BufferedImage> iconCache = new HashMap<>();
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
    private List<TrackerDisplay> trackerDisplays;
    private List<IndicatorState> spellStates;

    public PvMHUDOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setDragTargetable(true);
    }

    private void initializeTrackerDisplays() {
        if (trackerDisplays != null) {
            return;
        }

        trackerDisplays = List.of(
                new TrackerDisplay(thrallTracker, "T", IconRef.spellSprite(SpriteID.MagicNecroOn.RESURRECT_SUPERIOR_SKELETON), config::showThrall),
                new TrackerDisplay(deathChargeTracker, "D", IconRef.spellSprite(SpriteID.MagicNecroOn.DEATH_CHARGE), config::showDeathCharge),
                new TrackerDisplay(markOfDarknessTracker, "M", IconRef.spellSprite(SpriteID.MagicNecroOn.MARK_OF_DARKNESS), config::showMarkOfDarkness),
                new TrackerDisplay(vengeanceTracker, "V", IconRef.spellSprite(SpriteID.LunarMagicOn.VENGEANCE), config::showVengeance),
                new TrackerDisplay(corruptionTracker, "C", IconRef.spellSprite(SpriteID.MagicNecroOn.GREATER_CORRUPTION), config::showCorruption),
                new TrackerDisplay(wardOfArceuusTracker, "W", IconRef.spellSprite(SpriteID.MagicNecroOn.WARD_OF_ARCEUUS), config::showWardOfArceuus)
        );

        spellStates = new ArrayList<>(trackerDisplays.size());
        for (int i = 0; i < trackerDisplays.size(); i++) {
            spellStates.add(new IndicatorState());
        }
    }

    public void reset() {
        if (spellStates != null) {
            for (IndicatorState state : spellStates) {
                state.reset();
            }
        }
        heartState.reset();
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (client.getLocalPlayer() == null) {
            return null;
        }

        if (trackerDisplays == null) {
            initializeTrackerDisplays();
        }

        long now = System.nanoTime();

        for (int i = 0; i < trackerDisplays.size(); i++) {
            TrackerDisplay display = trackerDisplays.get(i);
            sync(display.tracker, spellStates.get(i), now);
        }
        sync(heartTracker, heartState, now);

        Font oldFont = g.getFont();
        g.setFont(resolveFont(oldFont));
        FontMetrics m = g.getFontMetrics();

        spellSegments.clear();
        statSegments.clear();
        heartSegments.clear();

        buildSpells(now);
        buildStats();
        buildHeart(now);

        if (spellSegments.isEmpty() && statSegments.isEmpty() && heartSegments.isEmpty()) {
            g.setFont(oldFont);
            return null;
        }

        Dimension styledDimension = renderStyled(g, m);
        if (styledDimension != null) {
            g.setFont(oldFont);
            return styledDimension;
        }

        boolean vertical = config.verticalLayout();
        int width;
        int height;

        if (vertical) {
            List<List<Segment>> cols = new ArrayList<>(2);
            if (!spellSegments.isEmpty()) cols.add(spellSegments);
            if (!statSegments.isEmpty()) cols.add(statSegments);

            int maxRows = 0;
            int totalW = 0;

            for (List<Segment> col : cols) {
                int cw = 0;
                for (Segment s : col) {
                    cw = Math.max(cw, segmentWidth(m, s));
                }
                totalW += cw;
                maxRows = Math.max(maxRows, col.size());
            }

            if (cols.size() > 1) {
                totalW += (cols.size() - 1) * groupGap();
            }

            int rowGap = rowGap();
            int[] rowHeights = rowHeights(m, cols, maxRows);
            int topHeight = sum(rowHeights) + (maxRows > 0 ? (maxRows - 1) * rowGap : 0);
            int heartWidth = rowWidth(m, heartSegments);
            int heartHeight = heartSegments.isEmpty() ? 0 : rowHeight(m, heartSegments);
            int heartGap = topHeight > 0 && heartHeight > 0 ? rowGap : 0;

            width = Math.max(totalW, heartWidth) + (PADDING_X * 2);
            height = topHeight + heartGap + heartHeight + (PADDING_Y * 2);

            drawBackground(g, width, height);

            int x = PADDING_X;

            for (List<Segment> col : cols) {
                int yOff = 0;
                int colW = 0;

                for (int row = 0; row < col.size(); row++) {
                    Segment s = col.get(row);
                    int rowHeight = rowHeights[row];
                    colW = Math.max(colW, segmentWidth(m, s));
                    drawSegment(g, m, s, x, baselineY(m, rowHeight) + yOff, rowHeight);
                    yOff += rowHeight + rowGap;
                }

                x += colW + groupGap();
            }

            if (!heartSegments.isEmpty()) {
                int heartY = topHeight + heartGap;
                drawCenteredRow(g, m, heartSegments, baselineY(m, heartHeight) + heartY, heartHeight, width);
            }
        } else {
            width = Math.max(rowWidth(m, spellSegments),
                    Math.max(rowWidth(m, statSegments), rowWidth(m, heartSegments)))
                    + (PADDING_X * 2);

            int rows = 0;
            if (!spellSegments.isEmpty()) rows++;
            if (!statSegments.isEmpty()) rows++;
            if (!heartSegments.isEmpty()) rows++;

            int spellHeight = spellSegments.isEmpty() ? 0 : rowHeight(m, spellSegments);
            int statHeight = statSegments.isEmpty() ? 0 : rowHeight(m, statSegments);
            int heartHeight = heartSegments.isEmpty() ? 0 : rowHeight(m, heartSegments);

            int rowGap = rowGap();
            height = spellHeight + statHeight + heartHeight + ((rows - 1) * rowGap) + (PADDING_Y * 2);

            drawBackground(g, width, height);

            int yOff = 0;

            if (!spellSegments.isEmpty()) {
                drawCenteredRow(g, m, spellSegments, baselineY(m, spellHeight) + yOff, spellHeight, width);
                yOff += spellHeight + rowGap;
            }
            if (!statSegments.isEmpty()) {
                drawCenteredRow(g, m, statSegments, baselineY(m, statHeight) + yOff, statHeight, width);
                yOff += statHeight + rowGap;
            }
            if (!heartSegments.isEmpty()) {
                drawCenteredRow(g, m, heartSegments, baselineY(m, heartHeight) + yOff, heartHeight, width);
            }
        }

        g.setFont(oldFont);
        return new Dimension(width, height);
    }

    private void buildSpells(long now) {
        for (int i = 0; i < trackerDisplays.size(); i++) {
            TrackerDisplay display = trackerDisplays.get(i);
            IndicatorState state = spellStates.get(i);

            if (display.isEnabled()) {
                addSpell(display, state, now);
            }
        }
    }

    private void addSpell(TrackerDisplay display, IndicatorState state, long now) {
        if (!shouldRender(state, now, config.showInactiveSpells())) return;

        spellSegments.add(new Segment(display.text, "", getColor(display.tracker, state, now), state, display.icon));
    }

    private void buildStats() {
        if (config.showHp()) {
            int hp = hpTracker.getCurrentHp();
            int poison = client.getVarpValue(GameStateIds.POISON);

            Color c;
            if (poison >= 1_000_000) c = config.venomedHpColor();
            else if (poison > 0) c = config.poisonedHpColor();
            else if (hp <= config.hpLowThreshold()) c = config.hpLowColor();
            else c = config.hpNormalColor();

            statSegments.add(new Segment("H " + hp, Integer.toString(hp), c, null, HITPOINTS_ICON));
        }

        if (config.showPrayer()) {
            int prayer = prayerTracker.getCurrentPrayer();
            statSegments.add(new Segment(
                    "P " + prayer,
                    Integer.toString(prayer),
                    prayer <= config.prayerLowThreshold()
                            ? config.prayerLowColor()
                            : config.prayerNormalColor(),
                    null,
                    PRAYER_ICON
            ));
        }

        if (config.showSpec()) {
            int spec = specTracker.getSpecPercent();
            statSegments.add(new Segment(
                    "S " + spec,
                    Integer.toString(spec),
                    spec >= config.specThreshold()
                            ? config.specHighColor()
                            : config.specLowColor(),
                    null,
                    SPEC_ICON
            ));
        }
    }

    private void buildHeart(long now) {
        if (config.showHeart()
                && shouldRender(heartState, now, true)) {
            heartSegments.add(new Segment("<3", "", getColor(heartTracker, heartState, now), heartState, HEART_ICON));
        }
    }

    private void sync(SpellStateTracker t, IndicatorState s, long now) {
        boolean active = t.isActive();
        boolean cooldown = t.isOnCooldown();
        boolean ready = !active && !cooldown;
        boolean expiringSoon = t.isExpiringSoon(config.spellExpiringSoonSeconds());

        boolean justBecameNotReady = s.wasReady && !ready;
        boolean justBecameReady = !s.wasReady && ready;
        boolean justExpired = s.wasExpiringSoon && !expiringSoon && !ready;

        if (justBecameNotReady) {
            s.lastCastNanos = now;
        }

        if (justBecameReady) {
            s.lastCastNanos = now;
        }

        if (justExpired) {
            s.lastCastNanos = now;
        }

        if (ready && s.wasReady && s.lastCastNanos > 0) {
            long flashDuration = config.readySpellFlashRecentSeconds() * TimeConstants.NS_PER_SECOND;
            if (now - s.lastCastNanos >= flashDuration) {
                s.lastCastNanos = 0;
            }
        }

        if (!ready) {
            s.lastSeenNanos = now;
        }

        s.wasReady = ready;
        s.wasExpiringSoon = expiringSoon;
        s.active = active;
        s.cooldown = cooldown;
        s.ready = ready;
        s.expiringSoon = expiringSoon;
    }

    private boolean shouldRender(IndicatorState s, long now, boolean allowInactive) {
        if (!s.ready) return true;

        return allowInactive
                && config.inactiveSpellTimeoutSeconds() > 0
                && s.lastSeenNanos > 0
                && now - s.lastSeenNanos <= config.inactiveSpellTimeoutSeconds() * TimeConstants.NS_PER_SECOND;
    }

    private Color getColor(SpellStateTracker t, IndicatorState s, long now) {
        if (s.expiringSoon)
            return config.expiringSpellColor();

        if (s.active)
            return activeColor(t);

        if (s.cooldown)
            return cooldownColor(t);

        if (shouldFlashReady(s, now) && isFlashPhase(now))
            return config.readySpellFlashColor();

        return config.readySpellColor();
    }

    private Color cooldownColor(SpellStateTracker t) {
        if (t == deathChargeTracker) {
            return config.deathChargeCooldownColor();
        }

        return config.cooldownSpellColor();
    }

    private boolean shouldFlashReady(IndicatorState s, long now) {
        if (!config.flashReadySpells()) return false;

        int window = config.readySpellFlashRecentSeconds();
        if (window == 0) return true;

        return s.lastCastNanos > 0
                && now - s.lastCastNanos <= window * TimeConstants.NS_PER_SECOND;
    }

    private boolean isFlashPhase(long now) {
        long period = Math.max(100L, config.flashPeriodMillis()) * TimeConstants.NS_PER_MS;
        return (now / period) % 2L == 0L;
    }

    private Color activeColor(SpellStateTracker t) {
        if (t == thrallTracker) return config.thrallActiveColor();
        if (t == markOfDarknessTracker) return config.markOfDarknessActiveColor();
        if (t == deathChargeTracker) return config.deathChargeActiveColor();
        if (t == vengeanceTracker) return config.vengeanceActiveColor();
        if (t == corruptionTracker) return config.corruptionActiveColor();
        if (t == wardOfArceuusTracker) return config.wardOfArceuusActiveColor();
        if (t == heartTracker) return config.heartActiveColor();

        return config.heartActiveColor();
    }

    private Dimension renderStyled(Graphics2D g, FontMetrics m) {
        switch (config.hudStyle()) {
            case BARS:
                return renderBars(g, m);
            case CHIPS:
                return renderChips(g, m);
            case ORBS:
                return renderOrbs(g, m);
            case STACK:
                return renderStack(g, m);
            default:
                return null;
        }
    }

    private Dimension renderChips(Graphics2D g, FontMetrics m) {
        List<Segment> spells = spellAndHeartSegments();
        int gap = Math.max(1, groupGap() / 2);
        int chipHeight = chipHeight(m);

        if (config.verticalLayout()) {
            int width = Math.max(maxChipWidth(m, statSegments), maxChipWidth(m, spells)) + PADDING_X * 2;
            int rows = statSegments.size() + spells.size();
            int height = rows * chipHeight + Math.max(0, rows - 1) * gap + PADDING_Y * 2;

            drawBackground(g, width, height);
            int y = PADDING_Y;
            for (Segment s : statSegments) {
                drawChip(g, m, s, PADDING_X, y, width - PADDING_X * 2, chipHeight);
                y += chipHeight + gap;
            }
            for (Segment s : spells) {
                drawChip(g, m, s, PADDING_X, y, width - PADDING_X * 2, chipHeight);
                y += chipHeight + gap;
            }
            return new Dimension(width, height);
        }

        int statWidth = chipsWidth(m, statSegments, gap);
        int spellWidth = chipsWidth(m, spells, gap);
        int width = Math.max(statWidth, spellWidth) + PADDING_X * 2;
        int rows = (statSegments.isEmpty() ? 0 : 1) + (spells.isEmpty() ? 0 : 1);
        int height = rows * chipHeight + Math.max(0, rows - 1) * rowGap() + PADDING_Y * 2;

        drawBackground(g, width, height);
        int y = PADDING_Y;
        if (!statSegments.isEmpty()) {
            drawChipRow(g, m, statSegments, y, width, statWidth, gap, chipHeight);
            y += chipHeight + rowGap();
        }
        if (!spells.isEmpty()) {
            drawChipRow(g, m, spells, y, width, spellWidth, gap, chipHeight);
        }
        return new Dimension(width, height);
    }

    private Dimension renderOrbs(Graphics2D g, FontMetrics m) {
        List<Segment> spells = spellAndHeartSegments();
        int gap = barGap();
        int orbSize = orbSize();
        int tileSize = barSpellTileSize();

        if (config.verticalLayout()) {
            int statHeight = statSegments.isEmpty() ? 0 : statSegments.size() * orbSize + Math.max(0, statSegments.size() - 1) * gap;
            int spellHeight = spells.isEmpty() ? 0 : spells.size() * tileSize + Math.max(0, spells.size() - 1) * gap;
            int colGap = !statSegments.isEmpty() && !spells.isEmpty() ? gap + 2 : 0;
            int width = (statSegments.isEmpty() ? 0 : orbSize) + colGap + (spells.isEmpty() ? 0 : tileSize) + PADDING_X * 2;
            int height = Math.max(statHeight, spellHeight) + PADDING_Y * 2;

            drawBackground(g, width, height);
            int statY = PADDING_Y + Math.max(0, (height - PADDING_Y * 2 - statHeight) / 2);
            for (Segment s : statSegments) {
                drawStatOrb(g, s, PADDING_X, statY, orbSize);
                statY += orbSize + gap;
            }
            int spellX = PADDING_X + (statSegments.isEmpty() ? 0 : orbSize + colGap);
            int spellY = PADDING_Y + Math.max(0, (height - PADDING_Y * 2 - spellHeight) / 2);
            for (Segment s : spells) {
                drawSpellTile(g, s, spellX, spellY, tileSize);
                spellY += tileSize + gap;
            }
            return new Dimension(width, height);
        }

        int statWidth = statSegments.isEmpty() ? 0 : statSegments.size() * orbSize + Math.max(0, statSegments.size() - 1) * gap;
        int spellWidth = spells.isEmpty() ? 0 : spells.size() * tileSize + Math.max(0, spells.size() - 1) * gap;
        int width = Math.max(statWidth, spellWidth) + PADDING_X * 2;
        int rows = (statSegments.isEmpty() ? 0 : 1) + (spells.isEmpty() ? 0 : 1);
        int height = (statSegments.isEmpty() ? 0 : orbSize) + (spells.isEmpty() ? 0 : tileSize)
                + Math.max(0, rows - 1) * gap + PADDING_Y * 2;

        drawBackground(g, width, height);
        int y = PADDING_Y;
        if (!statSegments.isEmpty()) {
            int x = PADDING_X + (width - PADDING_X * 2 - statWidth) / 2;
            for (Segment s : statSegments) {
                drawStatOrb(g, s, x, y, orbSize);
                x += orbSize + gap;
            }
            y += orbSize + gap;
        }
        if (!spells.isEmpty()) {
            int x = PADDING_X + (width - PADDING_X * 2 - spellWidth) / 2;
            for (Segment s : spells) {
                drawSpellTile(g, s, x, y, tileSize);
                x += tileSize + gap;
            }
        }
        return new Dimension(width, height);
    }

    private Dimension renderStack(Graphics2D g, FontMetrics m) {
        List<Segment> all = allSegments();

        int iconSize = Math.max(config.statIconSize(), config.spellIconSize());
        int rowHeight = Math.max(iconSize, m.getHeight());
        int gap = Math.max(0, rowGap());
        int width = 0;
        for (Segment s : all) {
            width = Math.max(width, iconSize + iconTextGap() + 6 + stackTextWidth(m, s));
        }
        width += PADDING_X * 2;
        int height = all.size() * rowHeight + Math.max(0, all.size() - 1) * gap + PADDING_Y * 2;

        drawBackground(g, width, height);
        int y = PADDING_Y;
        for (Segment s : all) {
            drawStackItem(g, m, s, PADDING_X, y, iconSize, rowHeight);
            y += rowHeight + gap;
        }
        return new Dimension(width, height);
    }

    private List<Segment> spellAndHeartSegments() {
        compactSpellSegments.clear();
        compactSpellSegments.addAll(spellSegments);
        compactSpellSegments.addAll(heartSegments);
        return compactSpellSegments;
    }

    private List<Segment> allSegments() {
        allSegments.clear();
        allSegments.addAll(statSegments);
        allSegments.addAll(spellSegments);
        allSegments.addAll(heartSegments);
        return allSegments;
    }

    private int maxChipWidth(FontMetrics m, List<Segment> segments) {
        int width = 0;
        for (Segment s : segments) {
            width = Math.max(width, chipWidth(m, s));
        }
        return width;
    }

    private int chipsWidth(FontMetrics m, List<Segment> segments, int gap) {
        if (segments.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (Segment s : segments) {
            width += chipWidth(m, s) + gap;
        }
        return width - gap;
    }

    private int chipWidth(FontMetrics m, Segment s) {
        if (!chipHasText(s)) {
            return chipIconSize(s) + 10;
        }

        return chipIconSize(s) + iconTextGap() + m.stringWidth(segmentLabel(s)) + 12;
    }

    private int chipHeight(FontMetrics m) {
        return Math.max(m.getHeight(), Math.max(config.statIconSize(), config.spellIconSize())) + 6;
    }

    private void drawChipRow(Graphics2D g, FontMetrics m, List<Segment> segments, int y, int width, int rowWidth, int gap, int chipHeight) {
        int x = PADDING_X + Math.max(0, (width - PADDING_X * 2 - rowWidth) / 2);
        for (Segment s : segments) {
            int chipWidth = chipWidth(m, s);
            drawChip(g, m, s, x, y, chipWidth, chipHeight);
            x += chipWidth + gap;
        }
    }

    private void drawChip(Graphics2D g, FontMetrics m, Segment s, int x, int y, int width, int height) {
        int alpha = config.backgroundAlpha();
        if (alpha > 0) {
            Color base = config.backgroundColor();
            g.setColor(withAlpha(base, alpha));
            g.fillRoundRect(x, y, width, height, 7, 7);
        }

        Color frame = s.color;
        boolean hasText = chipHasText(s);
        g.setColor(withAlpha(frame, Math.max(90, alpha)));
        g.drawRoundRect(x, y, width - 1, height - 1, 7, 7);
        if (hasText) {
            g.fillRoundRect(x + 1, y + 1, 3, height - 2, 4, 4);
        } else {
            g.fillRect(x + 3, y + height - 3, Math.max(1, width - 6), 2);
        }

        int iconSize = chipIconSize(s);
        int iconX = hasText ? x + 7 : x + (width - iconSize) / 2;
        int iconY = y + (height - iconSize) / 2;
        BufferedImage icon = loadScaledIcon(s.icon, iconSize);
        if (icon != null) {
            g.drawImage(icon, iconX, iconY, null);
        } else {
            drawText(g, firstLabelChar(s), iconX + 1, y + ((height - m.getHeight()) / 2) + m.getAscent(), m, s.color);
        }

        if (hasText) {
            drawText(g, segmentLabel(s), iconX + iconSize + iconTextGap(), y + ((height - m.getHeight()) / 2) + m.getAscent(), m, s.color);
        }
    }

    private int orbSize() {
        return Math.max(26, Math.max(config.statIconSize() + 14, config.fontSize() + 12));
    }

    private void drawStatOrb(Graphics2D g, Segment s, int x, int y, int size) {
        int alpha = config.backgroundAlpha();
        if (alpha > 0) {
            Color base = config.backgroundColor();
            g.setColor(withAlpha(base, alpha));
            g.fillOval(x, y, size, size);
        }

        Color fill = s.color;
        g.setColor(withAlpha(fill, Math.max(45, alpha / 2)));
        g.fillOval(x + 3, y + 3, Math.max(1, size - 6), Math.max(1, size - 6));
        g.setColor(withAlpha(fill, 220));
        g.drawOval(x, y, size - 1, size - 1);

        drawCenteredTextInBox(g, s.iconText, x + 2, y + 2, size - 4, size - 4, Color.WHITE, 7);
    }

    private int stackTextWidth(FontMetrics m, Segment s) {
        return m.stringWidth(segmentLabel(s));
    }

    private void drawStackItem(Graphics2D g, FontMetrics m, Segment s, int x, int y, int iconSize, int rowHeight) {
        BufferedImage icon = loadScaledIcon(s.icon, iconSize);
        int iconY = y + (rowHeight - iconSize) / 2;
        if (icon != null) {
            g.drawImage(icon, x, iconY, null);
        } else {
            drawText(g, firstLabelChar(s), x + 2, y + ((rowHeight - m.getHeight()) / 2) + m.getAscent(), m, s.color);
        }

        g.setColor(s.color);
        g.fillRect(x + iconSize + Math.max(1, iconTextGap() / 2), y + 2, 2, rowHeight - 4);
        drawText(g, segmentLabel(s), x + iconSize + iconTextGap() + 4, y + ((rowHeight - m.getHeight()) / 2) + m.getAscent(), m, s.color);
    }

    private String segmentLabel(Segment s) {
        return s.iconText.isEmpty() ? s.text : s.iconText;
    }

    private boolean chipHasText(Segment s) {
        return !s.iconText.isEmpty();
    }

    private String firstLabelChar(Segment s) {
        String label = segmentLabel(s);
        return label.isEmpty() ? "?" : label.substring(0, 1);
    }

    private int chipIconSize(Segment s) {
        if (s.icon != null && s.icon.group == IconGroup.STAT) {
            return config.statIconSize();
        }

        return config.spellIconSize();
    }

    private Dimension renderBars(Graphics2D g, FontMetrics m) {
        if (config.verticalLayout()) {
            return renderVerticalBars(g, m);
        }

        List<Segment> spells = spellAndHeartSegments();
        int spellCount = spells.size();
        int gap = barGap();
        int spellTileSize = barSpellTileSize();
        int spellWidth = spellCount == 0 ? 0 : spellCount * spellTileSize + (spellCount - 1) * gap;
        int statWidth = statSegments.isEmpty() ? 0 : config.statIconSize() + iconTextGap() + config.barWidth();
        int width = Math.max(spellWidth, statWidth) + (PADDING_X * 2);
        int contentWidth = width - PADDING_X * 2;
        int statRowHeight = horizontalStatRowHeight();

        int height = PADDING_Y * 2;
        if (spellCount > 0) {
            height += spellTileSize;
        }
        if (spellCount > 0 && !statSegments.isEmpty()) {
            height += gap + 1;
        }
        if (!statSegments.isEmpty()) {
            height += statSegments.size() * statRowHeight + (statSegments.size() - 1) * gap;
        }

        drawBarsBackground(g, width, height);

        int y = PADDING_Y;
        if (spellCount > 0) {
            int x = PADDING_X + (contentWidth - spellWidth) / 2;
            for (Segment s : spells) {
                drawSpellTile(g, s, x, y, spellTileSize);
                x += spellTileSize + gap;
            }
            y += spellTileSize + gap + 1;
        }

        int statX = PADDING_X + (contentWidth - statWidth) / 2;
        for (Segment s : statSegments) {
            drawStatBar(g, m, s, statX, y, statRowHeight);
            y += statRowHeight + gap;
        }

        return new Dimension(width, height);
    }

    private Dimension renderVerticalBars(Graphics2D g, FontMetrics m) {
        List<Segment> spells = spellAndHeartSegments();
        int statGroupWidth = verticalStatGroupWidth();
        int spellTileSize = barSpellTileSize();
        int spellCount = spells.size();
        int gap = barGap();
        int spellHeight = spellCount == 0 ? 0 : spellCount * spellTileSize + (spellCount - 1) * gap;
        int statWidth = statSegments.isEmpty() ? 0 : statSegments.size() * statGroupWidth + (statSegments.size() - 1) * gap;
        int statHeight = statSegments.isEmpty() ? 0 : config.statIconSize() + iconTextGap() + config.verticalBarHeight();
        int columnGap = spellCount > 0 && !statSegments.isEmpty() ? gap + 2 : 0;

        int spellWidth = spellCount > 0 ? spellTileSize : 0;
        int width = spellWidth + columnGap + statWidth + (PADDING_X * 2);
        int height = Math.max(spellHeight, statHeight) + (PADDING_Y * 2);
        int innerHeight = height - PADDING_Y * 2;

        drawBarsBackground(g, width, height);

        int spellY = PADDING_Y + Math.max(0, (innerHeight - spellHeight) / 2);
        int spellX = PADDING_X;
        for (Segment s : spells) {
            drawSpellTile(g, s, spellX, spellY, spellTileSize);
            spellY += spellTileSize + gap;
        }

        int statX = PADDING_X + (spellCount > 0 ? spellTileSize + columnGap : 0);
        int statY = PADDING_Y + Math.max(0, (innerHeight - statHeight) / 2);
        for (Segment s : statSegments) {
            drawVerticalStatBar(g, m, s, statX, statY, statGroupWidth);
            statX += statGroupWidth + gap;
        }

        return new Dimension(width, height);
    }

    private void drawSpellTile(Graphics2D g, Segment s, int x, int y, int size) {
        int alpha = config.backgroundAlpha();
        if (alpha > 0) {
            Color frame = darken(s.color, 0.55f);
            g.setColor(new Color(10, 12, 15, alpha));
            g.fillRoundRect(x, y, size, size, 4, 4);
            g.setColor(withAlpha(frame, alpha));
            g.drawRoundRect(x, y, size - 1, size - 1, 4, 4);
        }

        int iconSize = Math.max(8, size - 6);
        BufferedImage icon = loadScaledIcon(s.icon, iconSize);
        if (icon != null) {
            g.drawImage(icon, x + (size - icon.getWidth()) / 2, y + (size - icon.getHeight()) / 2 - 1, null);
        } else {
            String text = s.text;
            drawCenteredTextInBox(g, text, x + 2, y + 2, size - 4, size - 4, s.color, 7);
        }

        g.setColor(s.color);
        g.fillRect(x + 2, y + size - 3, size - 4, 2);
    }

    private void drawStatBar(Graphics2D g, FontMetrics m, Segment s, int x, int y, int rowHeight) {
        int iconSize = config.statIconSize();
        int barHeight = config.barHeight();
        int iconY = y + (rowHeight - iconSize) / 2;
        int barY = y + (rowHeight - barHeight) / 2;
        BufferedImage icon = loadScaledIcon(s.icon, iconSize);
        if (icon != null) {
            g.drawImage(icon, x, iconY, null);
        } else {
            drawText(g, firstLabelChar(s), x + 2, iconY + m.getAscent(), m, s.color);
        }

        int barX = x + iconSize + iconTextGap();
        int value = parseValue(s.iconText);
        int max = maxValueFor(s);
        int barWidth = config.barWidth();
        int fillWidth = max <= 0 ? barWidth : Math.max(0, Math.min(barWidth, (int) Math.round(barWidth * Math.min(value, max) / (double) max)));

        g.setColor(new Color(8, 10, 13, 220));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);

        Color fill = s.color;
        g.setColor(withAlpha(fill, 230));
        g.fillRoundRect(barX, barY, fillWidth, barHeight, 4, 4);

        g.setColor(new Color(255, 255, 255, 28));
        g.fillRect(barX + 1, barY + 1, Math.max(0, barWidth - 2), Math.max(1, barHeight / 3));

        g.setColor(new Color(0, 0, 0, 190));
        g.drawRoundRect(barX, barY, barWidth - 1, barHeight - 1, 4, 4);

        drawCenteredTextInBox(g, s.iconText, barX + 1, barY, barWidth - 2, barHeight, Color.WHITE, 6);
    }

    private void drawVerticalStatBar(Graphics2D g, FontMetrics m, Segment s, int x, int y, int groupWidth) {
        int iconSize = config.statIconSize();
        int barWidth = verticalMeterWidth();
        int barX = x + (groupWidth - barWidth) / 2;
        BufferedImage icon = loadScaledIcon(s.icon, iconSize);
        int iconX = x + (groupWidth - iconSize) / 2;
        if (icon != null) {
            g.drawImage(icon, iconX, y, null);
        } else {
            drawText(g, firstLabelChar(s), iconX + 2, y + m.getAscent(), m, s.color);
        }

        int barY = y + iconSize + iconTextGap();
        int value = parseValue(s.iconText);
        int max = maxValueFor(s);
        int barHeight = config.verticalBarHeight();
        int fillHeight = max <= 0 ? barHeight : Math.max(0, Math.min(barHeight, (int) Math.round(barHeight * Math.min(value, max) / (double) max)));
        int fillY = barY + barHeight - fillHeight;

        g.setColor(new Color(8, 10, 13, 220));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);

        Color fill = s.color;
        g.setColor(withAlpha(fill, 230));
        g.fillRoundRect(barX, fillY, barWidth, fillHeight, 4, 4);

        g.setColor(new Color(255, 255, 255, 28));
        g.fillRect(barX + 1, barY + 1, Math.max(0, barWidth - 2), Math.max(1, barHeight / 5));

        g.setColor(new Color(0, 0, 0, 190));
        g.drawRoundRect(barX, barY, barWidth - 1, barHeight - 1, 4, 4);

        if (config.verticalBarText()) {
            drawVerticalTextInBar(g, s.iconText, barX, barY, barWidth, barHeight, Color.WHITE);
        } else {
            int tx = x + (groupWidth - m.stringWidth(s.iconText)) / 2;
            int ty = barY + barHeight - 4;
            drawText(g, s.iconText, tx, ty, m, Color.WHITE);
        }
    }

    private int verticalStatGroupWidth() {
        return Math.max(verticalMeterWidth(), config.statIconSize());
    }

    private int verticalMeterWidth() {
        return config.verticalBarWidth();
    }

    private int barSpellTileSize() {
        return config.barSpellTileSize();
    }

    private int horizontalStatRowHeight() {
        return Math.max(config.barHeight(), config.statIconSize());
    }

    private int barGap() {
        return config.barGap();
    }

    private int groupGap() {
        return config.groupGap();
    }

    private int rowGap() {
        return config.rowGap();
    }

    private int iconTextGap() {
        return config.iconTextGap();
    }

    private void drawVerticalTextInBar(Graphics2D g, String text, int x, int y, int width, int height, Color color) {
        if (text.isEmpty() || width <= 0 || height <= 0) {
            return;
        }

        Font font = g.getFont();
        FontMetrics metrics = g.getFontMetrics(font);
        int maxTextHeight = Math.max(1, height - 4);
        int maxTextWidth = Math.max(1, width - 2);

        while (font.getSize() > 6
                && (metrics.stringWidth(text) > maxTextHeight || metrics.getHeight() > maxTextWidth)) {
            font = font.deriveFont((float) font.getSize() - 1F);
            metrics = g.getFontMetrics(font);
        }

        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int textX = centerX - metrics.stringWidth(text) / 2;
        int textY = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;

        Graphics2D rotated = (Graphics2D) g.create();
        try {
            rotated.setClip(x, y, width, height);
            rotated.setFont(font);
            rotated.rotate(-Math.PI / 2, centerX, centerY);
            drawText(rotated, text, textX, textY, metrics, color);
        } finally {
            rotated.dispose();
        }
    }

    private void drawBarsBackground(Graphics2D g, int w, int h) {
        int alpha = config.backgroundAlpha();
        if (alpha <= 0) return;

        Color base = config.backgroundColor();
        g.setColor(withAlpha(base, alpha));
        g.fillRoundRect(0, 0, w, h, 7, 7);
        g.setColor(new Color(255, 255, 255, 22));
        g.drawRoundRect(0, 0, w - 1, h - 1, 7, 7);
    }

    private int parseValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int maxValueFor(Segment s) {
        if (s.icon == null) {
            return 100;
        }

        if (s.icon.type == IconType.SPRITE && s.icon.id == SpriteID.OrbIcon.SPECIAL) {
            return 100;
        }

        if (s.icon.type == IconType.SKILL && s.icon.id >= 0 && s.icon.id < SKILLS.length) {
            return Math.max(1, client.getRealSkillLevel(SKILLS[s.icon.id]));
        }

        return 100;
    }

    private Color darken(Color color, float amount) {
        return new Color(
                Math.max(0, (int) (color.getRed() * amount)),
                Math.max(0, (int) (color.getGreen() * amount)),
                Math.max(0, (int) (color.getBlue() * amount))
        );
    }

    private Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private void drawCenteredRow(Graphics2D g, FontMetrics m, List<Segment> list, int y, int lineHeight, int totalWidth) {
        int availableWidth = Math.max(0, totalWidth - PADDING_X * 2);
        int x = PADDING_X + Math.max(0, (availableWidth - rowWidth(m, list)) / 2);
        drawRowAt(g, m, list, x, y, lineHeight);
    }

    private void drawRowAt(Graphics2D g, FontMetrics m, List<Segment> list, int x, int y, int lineHeight) {
        for (Segment s : list) {
            drawSegment(g, m, s, x, y, lineHeight);
            x += segmentWidth(m, s) + groupGap();
        }
    }

    private void drawSegment(Graphics2D g, FontMetrics m, Segment s, int x, int y, int lineHeight) {
        BufferedImage icon = getSegmentIcon(s);
        if (icon == null) {
            drawText(g, s.text, x, y, m, s.color);
            return;
        }

        int iconY = y - m.getAscent() - ((lineHeight - m.getHeight()) / 2) + ((lineHeight - icon.getHeight()) / 2);
        g.drawImage(icon, x, iconY, null);

        if (!s.iconText.isEmpty()) {
            drawText(g, s.iconText, x + icon.getWidth() + iconTextGap(), y, m, s.color);
        } else if (s.state != null) {
            g.setColor(s.color);
            g.fillRect(x, iconY + icon.getHeight() - 2, icon.getWidth(), 2);
        }
    }

    private void drawText(Graphics2D g, String text, int x, int y, FontMetrics m, Color color) {
        if (config.textOutline()) {
            g.setColor(config.outlineColor());
            g.drawString(text, x - 1, y);
            g.drawString(text, x + 1, y);
            g.drawString(text, x, y - 1);
            g.drawString(text, x, y + 1);
        }

        if (config.textShadow()) {
            g.setColor(config.shadowColor());
            g.drawString(text, x + 1, y + 1);
        }

        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void drawCenteredTextInBox(Graphics2D g, String text, int x, int y, int width, int height, Color color, int minFontSize) {
        if (text.isEmpty() || width <= 0 || height <= 0) {
            return;
        }

        Font originalFont = g.getFont();
        Font font = originalFont;
        FontMetrics metrics = g.getFontMetrics(font);
        int maxTextWidth = Math.max(1, width);

        while (font.getSize() > minFontSize
                && (metrics.stringWidth(text) > maxTextWidth || metrics.getHeight() > height + 2)) {
            font = font.deriveFont((float) font.getSize() - 1F);
            metrics = g.getFontMetrics(font);
        }

        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();

        g.setFont(font);
        drawText(g, text, textX, textY, metrics, color);
        g.setFont(originalFont);
    }

    private int rowWidth(FontMetrics m, List<Segment> list) {
        if (list.isEmpty()) return 0;

        int w = 0;
        for (Segment s : list) {
            w += segmentWidth(m, s) + groupGap();
        }
        return w - groupGap();
    }

    private int segmentWidth(FontMetrics m, Segment s) {
        BufferedImage icon = getSegmentIcon(s);
        if (icon == null) {
            return m.stringWidth(s.text);
        }

        int textWidth = s.iconText.isEmpty() ? 0 : iconTextGap() + m.stringWidth(s.iconText);
        return icon.getWidth() + textWidth;
    }

    private int rowHeight(FontMetrics m, List<Segment> segments) {
        int height = m.getHeight();
        for (Segment segment : segments) {
            height = Math.max(height, segmentHeight(segment));
        }
        return height;
    }

    private int segmentHeight(Segment segment) {
        BufferedImage icon = getSegmentIcon(segment);
        return icon == null ? 0 : icon.getHeight();
    }

    private int[] rowHeights(FontMetrics m, List<List<Segment>> cols, int maxRows) {
        int[] heights = new int[maxRows];
        for (int row = 0; row < maxRows; row++) {
            heights[row] = m.getHeight();
            for (List<Segment> col : cols) {
                if (row < col.size()) {
                    heights[row] = Math.max(heights[row], segmentHeight(col.get(row)));
                }
            }
        }
        return heights;
    }

    private int sum(int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    private int baselineY(FontMetrics m, int lineHeight) {
        return PADDING_Y + ((lineHeight - m.getHeight()) / 2) + m.getAscent();
    }

    private BufferedImage getSegmentIcon(Segment s) {
        if (config.hudStyle() != HudStyle.GAME_ICONS || s.icon == null) {
            return null;
        }

        return loadScaledIcon(s.icon, iconSize(s.icon));
    }

    private BufferedImage loadScaledIcon(IconRef iconRef, int iconSize) {
        if (iconRef == null) {
            return null;
        }

        long cacheKey = iconCacheKey(iconRef, iconSize);
        BufferedImage cached = iconCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        BufferedImage raw = loadIcon(iconRef);
        if (raw == null) {
            return null;
        }

        BufferedImage icon = ImageUtil.resizeCanvas(ImageUtil.resizeImage(raw, iconSize, iconSize, true), iconSize, iconSize);

        iconCache.put(cacheKey, icon);
        return icon;
    }

    private long iconCacheKey(IconRef iconRef, int iconSize) {
        return ((long) iconRef.group.ordinal() << 56)
                | ((long) iconRef.type.ordinal() << 48)
                | ((long) iconSize << 32)
                | (iconRef.id & 0xFFFFFFFFL);
    }

    private BufferedImage loadIcon(IconRef icon) {
        switch (icon.type) {
            case SPRITE:
                return spriteManager.getSprite(icon.id, 0);
            case ITEM:
                return itemManager.getImage(icon.id);
            case SKILL:
                if (icon.id < 0 || icon.id >= SKILLS.length) {
                    return null;
                }
                return skillIconManager.getSkillImage(SKILLS[icon.id], true);
            default:
                return null;
        }
    }

    private int iconSize(IconRef icon) {
        switch (icon.group) {
            case STAT:
                return config.statIconSize();
            case SPELL:
            default:
                return config.spellIconSize();
        }
    }

    private void drawBackground(Graphics2D g, int w, int h) {
        int a = config.backgroundAlpha();
        if (a <= 0) return;

        Color c = config.backgroundColor();
        g.setColor(withAlpha(c, a));
        g.fillRoundRect(0, 0, w, h, 8, 8);
    }

    private Font resolveFont(Font fallback) {
        switch (config.fontType()) {
            case RUNESCAPE:
                return FontManager.getRunescapeFont().deriveFont((float) config.fontSize());
            case RUNESCAPE_BOLD:
                return FontManager.getRunescapeBoldFont().deriveFont((float) config.fontSize());
            case RUNESCAPE_SMALL:
                return FontManager.getRunescapeSmallFont().deriveFont((float) config.fontSize());
            default:
                return fallback.deriveFont(
                        config.boldFont() ? Font.BOLD : Font.PLAIN,
                        (float) config.fontSize()
                );
        }
    }

    private static final class IndicatorState {
        long lastSeenNanos;
        long lastCastNanos;
        boolean wasReady = true;
        boolean wasExpiringSoon = false;
        boolean active;
        boolean cooldown;
        boolean ready = true;
        boolean expiringSoon;

        void reset() {
            lastSeenNanos = 0;
            lastCastNanos = 0;
            wasReady = true;
            wasExpiringSoon = false;
            active = false;
            cooldown = false;
            ready = true;
            expiringSoon = false;
        }
    }

    private static final class Segment {
        final String text;
        final String iconText;
        final Color color;
        final IndicatorState state;
        final IconRef icon;

        Segment(String text, String iconText, Color color, IndicatorState state, IconRef icon) {
            this.text = text;
            this.iconText = iconText;
            this.color = color;
            this.state = state;
            this.icon = icon;
        }
    }

    private static final class TrackerDisplay {
        final SpellStateTracker tracker;
        final String text;
        final IconRef icon;
        final java.util.function.BooleanSupplier enabledCheck;

        TrackerDisplay(SpellStateTracker tracker, String text, IconRef icon, java.util.function.BooleanSupplier enabledCheck) {
            this.tracker = tracker;
            this.text = text;
            this.icon = icon;
            this.enabledCheck = enabledCheck;
        }

        boolean isEnabled() {
            return enabledCheck.getAsBoolean();
        }
    }

    private enum IconType {
        SPRITE,
        ITEM,
        SKILL
    }

    private enum IconGroup {
        SPELL,
        STAT
    }

    private static final class IconRef {
        final IconType type;
        final IconGroup group;
        final int id;

        private IconRef(IconType type, IconGroup group, int id) {
            this.type = type;
            this.group = group;
            this.id = id;
        }

        static IconRef spellSprite(int spriteId) {
            return new IconRef(IconType.SPRITE, IconGroup.SPELL, spriteId);
        }

        static IconRef spellItem(int itemId) {
            return new IconRef(IconType.ITEM, IconGroup.SPELL, itemId);
        }

        static IconRef statSprite(int spriteId) {
            return new IconRef(IconType.SPRITE, IconGroup.STAT, spriteId);
        }

        static IconRef statSkill(Skill skill) {
            return new IconRef(IconType.SKILL, IconGroup.STAT, skill.ordinal());
        }
    }
}
