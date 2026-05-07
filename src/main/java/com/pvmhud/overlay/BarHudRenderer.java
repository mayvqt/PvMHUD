package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Singleton
final class BarHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        var spells = frame.spells();
        var hearts = frame.hearts();
        int spellCount = spells.size() + hearts.size();
        int gap = barGap();
        int tile = barSpellTileSize();
        int statIconSize = config.statIconSize();
        int barHeight = config.barHeight();
        int barWidth = config.barWidth();
        int spellWidth = spellCount == 0 ? 0 : spellCount * tile + (spellCount - 1) * gap;

        if (config.verticalLayout()) {
            return renderVertical(graphics, metrics, frame, spells, hearts, gap, tile);
        }

        int statWidth = frame.stats().isEmpty() ? 0 : statIconSize + iconTextGap() + barWidth;
        int width = Math.max(spellWidth, statWidth) + HudConstants.PADDING_X * 2;
        int rowHeight = Math.max(statIconSize, barHeight);
        int height = HudConstants.PADDING_Y * 2
                + (spellCount == 0 ? 0 : tile)
                + (spellCount > 0 && !frame.stats().isEmpty() ? gap + 1 : 0)
                + (frame.stats().isEmpty() ? 0 : frame.stats().size() * rowHeight + Math.max(0, frame.stats().size() - 1) * gap);

        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        if (spellCount > 0) {
            int x = centeredStartX(width, spellWidth);
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, x, y, tile);
                x += tile + gap;
            }
            for (Segment segment : hearts) {
                drawSpellTile(graphics, segment, x, y, tile);
                x += tile + gap;
            }
            y += tile + gap + 1;
        }

        int statX = centeredStartX(width, statWidth);
        for (Segment segment : frame.stats()) {
            drawHorizontalStatBar(graphics, metrics, segment, statX, y, rowHeight);
            y += rowHeight + gap;
        }

        return new Dimension(width, height);
    }

    private Dimension renderVertical(Graphics2D graphics, FontMetrics metrics, HudFrame frame, java.util.List<Segment> spells, java.util.List<Segment> hearts, int gap, int tile) {
        int statIconSize = config.statIconSize();
        int verticalBarWidth = Math.max(1, config.verticalBarWidth());
        int verticalBarHeight = config.verticalBarHeight();
        int barWidth = verticalBarWidth;
        int statColumnWidth = frame.stats().isEmpty()
                ? 0
                : frame.stats().size() * Math.max(statIconSize, barWidth) + Math.max(0, frame.stats().size() - 1) * gap;
        int spellCount = spells.size() + hearts.size();
        int spellHeight = spellCount == 0 ? 0 : spellCount * tile + Math.max(0, spellCount - 1) * gap;
        int statHeight = frame.stats().isEmpty() ? 0 : statIconSize + iconTextGap() + verticalBarHeight;
        int columnGap = spellCount > 0 && !frame.stats().isEmpty() ? gap + 2 : 0;

        int width = (spellCount == 0 ? 0 : tile) + columnGap + statColumnWidth + HudConstants.PADDING_X * 2;
        int height = Math.max(spellHeight, statHeight) + HudConstants.PADDING_Y * 2;
        text.drawBackground(graphics, width, height);

        int spellY = HudConstants.PADDING_Y + Math.max(0, (height - HudConstants.PADDING_Y * 2 - spellHeight) / 2);
        for (Segment segment : spells) {
            drawSpellTile(graphics, segment, HudConstants.PADDING_X, spellY, tile);
            spellY += tile + gap;
        }
        for (Segment segment : hearts) {
            drawSpellTile(graphics, segment, HudConstants.PADDING_X, spellY, tile);
            spellY += tile + gap;
        }

        int x = HudConstants.PADDING_X + (spellCount == 0 ? 0 : tile + columnGap);
        int y = HudConstants.PADDING_Y + Math.max(0, (height - HudConstants.PADDING_Y * 2 - statHeight) / 2);
        for (Segment segment : frame.stats()) {
            int columnWidth = Math.max(statIconSize, barWidth);
            drawVerticalStatBar(graphics, metrics, segment, x, y, columnWidth, statHeight);
            x += columnWidth + gap;
        }

        return new Dimension(width, height);
    }

    private void drawHorizontalStatBar(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int height) {
        int iconSize = config.statIconSize();
        BufferedImage icon = icons.load(segment.icon, iconSize);
        if (icon != null) {
            graphics.drawImage(icon, x, y + (height - iconSize) / 2, null);
        }

        int barX = x + iconSize + iconTextGap();
        int barY = y + Math.max(0, (height - config.barHeight()) / 2);
        int barWidth = config.barWidth();
        int barHeight = config.barHeight();

        graphics.setColor(text.withAlpha(config.backgroundColor(), Math.max(40, config.backgroundAlpha())));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        graphics.setColor(text.withAlpha(segment.color, 180));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        graphics.setColor(Color.WHITE);
        text.drawCenteredText(graphics, metrics, segment.label(), barX, barY, barWidth, barHeight);
    }

    private void drawVerticalStatBar(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int width, int height) {
        int iconSize = config.statIconSize();
        BufferedImage icon = icons.load(segment.icon, iconSize);
        if (icon != null) {
            graphics.drawImage(icon, x + (width - iconSize) / 2, y, null);
        }

        int barY = y + iconSize + iconTextGap();
        int barHeight = config.verticalBarHeight();
        int barWidth = Math.max(1, config.verticalBarWidth());
        int barX = x + (width - barWidth) / 2;

        graphics.setColor(text.withAlpha(config.backgroundColor(), Math.max(40, config.backgroundAlpha())));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        graphics.setColor(text.withAlpha(segment.color, 180));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);

        if (config.verticalBarText()) {
            graphics.setColor(Color.WHITE);
            text.drawCenteredText(graphics, metrics, segment.label(), x, barY, width, barHeight);
        }
    }

    private int barSpellTileSize() {
        return Math.max(14, config.barSpellTileSize());
    }
}
