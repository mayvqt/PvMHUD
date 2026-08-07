package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

@Singleton
final class PlayerBoundHudRenderer extends AbstractHudRenderer {
    private static final int PADDING = 4;
    private static final int SIDE_GAP = 8;
    // Stat offsets were originally measured from the player's feet. Keep existing
    // configurations visually close while anchoring the text to the model's head.
    private static final int LEGACY_STAT_Y_ORIGIN = 55;

    void render(Graphics2D graphics, FontMetrics metrics, HudFrame frame,
                Point playerHead, Point playerLowerBody) {
        drawStats(graphics, metrics, frame.stats(), playerHead);
        drawSpells(graphics, frame.spells(), frame.hearts(), playerLowerBody);
    }

    private void drawStats(Graphics2D graphics, FontMetrics metrics, List<Segment> stats, Point playerLocation) {
        if (stats.isEmpty()) {
            return;
        }

        int rowHeight = metrics.getHeight();
        int gap = rowGap();
        int baseline = text.baseline(metrics, rowHeight);
        int contentWidth = 0;
        for (Segment segment : stats) {
            contentWidth = Math.max(contentWidth, metrics.stringWidth(segment.iconText));
        }

        int width = contentWidth + PADDING * 2;
        int height = stats.size() * rowHeight + Math.max(0, stats.size() - 1) * gap + PADDING * 2;
        Point location = statLocation(playerLocation, width, config.playerStatSide(),
                config.playerStatOffsetX(), config.playerStatOffsetY());

        Graphics2D local = (Graphics2D) graphics.create();
        local.translate(location.x, location.y);
        text.drawBackground(local, width, height);

        int rowY = PADDING;
        for (Segment segment : stats) {
            text.drawText(local, segment.iconText, PADDING,
                    rowY + baseline, segment.color);
            rowY += rowHeight + gap;
        }
        local.dispose();
    }

    private void drawSpells(Graphics2D graphics, List<Segment> spells, List<Segment> hearts, Point playerLocation) {
        int count = spells.size() + hearts.size();
        if (count == 0) {
            return;
        }

        int size = config.spellIconSize();
        int gap = groupGap();
        PlayerSpellPosition position = config.playerSpellPosition();
        boolean horizontal = position == PlayerSpellPosition.BELOW;
        int width = horizontal ? count * size + (count - 1) * gap : size;
        int height = horizontal ? size : count * size + (count - 1) * gap;
        Point location = spellLocation(playerLocation, width, height, position,
                config.playerSpellOffsetX(), config.playerSpellOffsetY());
        int stepX = horizontal ? size + gap : 0;
        int stepY = horizontal ? 0 : size + gap;
        int nextIndex = drawSpellSegments(graphics, spells, location, size, stepX, stepY, 0);
        drawSpellSegments(graphics, hearts, location, size, stepX, stepY, nextIndex);
    }

    private int drawSpellSegments(Graphics2D graphics, List<Segment> segments, Point location,
                                  int size, int stepX, int stepY, int startIndex) {
        int index = startIndex;
        for (Segment segment : segments) {
            drawSpellTile(graphics, segment,
                    location.x + index * stepX,
                    location.y + index * stepY,
                    size);
            index++;
        }
        return index;
    }

    static Point statLocation(Point player, int width, PlayerStatSide side, int offsetX, int offsetY) {
        int x = side == PlayerStatSide.RIGHT
                ? player.x + offsetX
                : player.x - width - offsetX;
        return new Point(x, player.y + offsetY + LEGACY_STAT_Y_ORIGIN);
    }

    static Point spellLocation(Point player, int width, int height, PlayerSpellPosition position,
                               int offsetX, int offsetY) {
        switch (position) {
            case LEFT:
                return new Point(player.x - width - SIDE_GAP + offsetX,
                        player.y - height / 2 + offsetY);
            case RIGHT:
                return new Point(player.x + SIDE_GAP + offsetX,
                        player.y - height / 2 + offsetY);
            case BELOW:
            default:
                return new Point(player.x - width / 2 + offsetX,
                        player.y + offsetY);
        }
    }
}
