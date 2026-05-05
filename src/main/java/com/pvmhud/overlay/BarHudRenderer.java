package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

@Singleton
final class BarHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        List<Segment> spells = frame.spellsAndHearts();
        int gap = barGap();
        int tile = barSpellTileSize();
        int spellWidth = spells.isEmpty() ? 0 : spells.size() * tile + (spells.size() - 1) * gap;

        if (config.verticalLayout()) {
            return renderVertical(graphics, metrics, frame, spells, gap, tile);
        }

        int statWidth = frame.stats().isEmpty() ? 0 : config.statIconSize() + iconTextGap() + config.barWidth();
        int width = Math.max(spellWidth, statWidth) + HudConstants.PADDING_X * 2;
        int rowHeight = Math.max(config.statIconSize(), config.barHeight());
        int height = HudConstants.PADDING_Y * 2
                + (spells.isEmpty() ? 0 : tile)
                + (!spells.isEmpty() && !frame.stats().isEmpty() ? gap + 1 : 0)
                + (frame.stats().isEmpty() ? 0 : frame.stats().size() * rowHeight + Math.max(0, frame.stats().size() - 1) * gap);

        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        if (!spells.isEmpty()) {
            int x = HudConstants.PADDING_X + (width - HudConstants.PADDING_X * 2 - spellWidth) / 2;
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, x, y, tile);
                x += tile + gap;
            }
            y += tile + gap + 1;
        }

        int statX = HudConstants.PADDING_X + (width - HudConstants.PADDING_X * 2 - statWidth) / 2;
        for (Segment segment : frame.stats()) {
            drawHorizontalStatBar(graphics, metrics, segment, statX, y, rowHeight);
            y += rowHeight + gap;
        }

        return new Dimension(width, height);
    }

    private Dimension renderVertical(Graphics2D graphics, FontMetrics metrics, HudFrame frame, List<Segment> spells, int gap, int tile) {
        int barWidth = Math.max(1, config.verticalBarWidth());
        int statColumnWidth = frame.stats().isEmpty()
                ? 0
                : frame.stats().size() * Math.max(config.statIconSize(), barWidth) + Math.max(0, frame.stats().size() - 1) * gap;
        int spellHeight = spells.isEmpty() ? 0 : spells.size() * tile + Math.max(0, spells.size() - 1) * gap;
        int statHeight = frame.stats().isEmpty() ? 0 : config.statIconSize() + iconTextGap() + config.verticalBarHeight();
        int columnGap = !spells.isEmpty() && !frame.stats().isEmpty() ? gap + 2 : 0;

        int width = (spells.isEmpty() ? 0 : tile) + columnGap + statColumnWidth + HudConstants.PADDING_X * 2;
        int height = Math.max(spellHeight, statHeight) + HudConstants.PADDING_Y * 2;
        text.drawBackground(graphics, width, height);

        int spellY = HudConstants.PADDING_Y + Math.max(0, (height - HudConstants.PADDING_Y * 2 - spellHeight) / 2);
        for (Segment segment : spells) {
            drawSpellTile(graphics, segment, HudConstants.PADDING_X, spellY, tile);
            spellY += tile + gap;
        }

        int x = HudConstants.PADDING_X + (spells.isEmpty() ? 0 : tile + columnGap);
        int y = HudConstants.PADDING_Y + Math.max(0, (height - HudConstants.PADDING_Y * 2 - statHeight) / 2);
        for (Segment segment : frame.stats()) {
            drawVerticalStatBar(graphics, metrics, segment, x, y, Math.max(config.statIconSize(), barWidth), statHeight);
            x += Math.max(config.statIconSize(), barWidth) + gap;
        }

        return new Dimension(width, height);
    }

    private void drawSpellTile(Graphics2D graphics, Segment segment, int x, int y, int size) {
        int alpha = config.backgroundAlpha();
        graphics.setColor(text.withAlpha(segment.color, Math.max(55, alpha / 2)));
        graphics.fillRoundRect(x, y, size, size, 6, 6);
        graphics.setColor(text.withAlpha(segment.color, 220));
        graphics.drawRoundRect(x, y, size - 1, size - 1, 6, 6);

        BufferedImage icon = icons.load(segment.icon, Math.max(10, size - 6));
        if (icon != null) {
            int iconX = x + (size - icon.getWidth()) / 2;
            int iconY = y + (size - icon.getHeight()) / 2;
            graphics.drawImage(icon, iconX, iconY, null);
        }
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
