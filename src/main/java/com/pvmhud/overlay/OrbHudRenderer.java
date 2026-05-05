package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

@Singleton
final class OrbHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        List<Segment> spells = frame.spellsAndHearts();
        int gap = barGap();
        int orb = orbSize();
        int tile = Math.max(14, config.barSpellTileSize());

        if (config.verticalLayout()) {
            return renderVertical(graphics, metrics, frame, spells, gap, orb, tile);
        }

        int statWidth = frame.stats().isEmpty() ? 0 : frame.stats().size() * orb + Math.max(0, frame.stats().size() - 1) * gap;
        int spellWidth = spells.isEmpty() ? 0 : spells.size() * tile + Math.max(0, spells.size() - 1) * gap;
        int width = Math.max(statWidth, spellWidth) + HudConstants.PADDING_X * 2;
        int rows = (frame.stats().isEmpty() ? 0 : 1) + (spells.isEmpty() ? 0 : 1);
        int height = (frame.stats().isEmpty() ? 0 : orb) + (spells.isEmpty() ? 0 : tile)
                + Math.max(0, rows - 1) * gap + HudConstants.PADDING_Y * 2;

        text.drawBackground(graphics, width, height);

        int y = HudConstants.PADDING_Y;
        if (!frame.stats().isEmpty()) {
            int x = HudConstants.PADDING_X + (width - HudConstants.PADDING_X * 2 - statWidth) / 2;
            for (Segment segment : frame.stats()) {
                drawStatOrb(graphics, metrics, segment, x, y, orb);
                x += orb + gap;
            }
            y += orb + gap;
        }

        if (!spells.isEmpty()) {
            int x = HudConstants.PADDING_X + (width - HudConstants.PADDING_X * 2 - spellWidth) / 2;
            for (Segment segment : spells) {
                drawSpellTile(graphics, segment, x, y, tile);
                x += tile + gap;
            }
        }

        return new Dimension(width, height);
    }

    private Dimension renderVertical(Graphics2D graphics, FontMetrics metrics, HudFrame frame, List<Segment> spells, int gap, int orb, int tile) {
        int statHeight = frame.stats().isEmpty() ? 0 : frame.stats().size() * orb + Math.max(0, frame.stats().size() - 1) * gap;
        int spellHeight = spells.isEmpty() ? 0 : spells.size() * tile + Math.max(0, spells.size() - 1) * gap;
        int columnGap = !frame.stats().isEmpty() && !spells.isEmpty() ? gap + 2 : 0;
        int width = (frame.stats().isEmpty() ? 0 : orb) + columnGap + (spells.isEmpty() ? 0 : tile) + HudConstants.PADDING_X * 2;
        int height = Math.max(statHeight, spellHeight) + HudConstants.PADDING_Y * 2;

        text.drawBackground(graphics, width, height);

        int statY = HudConstants.PADDING_Y + Math.max(0, (height - HudConstants.PADDING_Y * 2 - statHeight) / 2);
        for (Segment segment : frame.stats()) {
            drawStatOrb(graphics, metrics, segment, HudConstants.PADDING_X, statY, orb);
            statY += orb + gap;
        }

        int spellX = HudConstants.PADDING_X + (frame.stats().isEmpty() ? 0 : orb + columnGap);
        int spellY = HudConstants.PADDING_Y + Math.max(0, (height - HudConstants.PADDING_Y * 2 - spellHeight) / 2);
        for (Segment segment : spells) {
            drawSpellTile(graphics, segment, spellX, spellY, tile);
            spellY += tile + gap;
        }

        return new Dimension(width, height);
    }

    private void drawStatOrb(Graphics2D graphics, FontMetrics metrics, Segment segment, int x, int y, int size) {
        graphics.setColor(text.withAlpha(segment.color, Math.max(45, config.backgroundAlpha() / 2)));
        graphics.fillOval(x + 3, y + 3, Math.max(1, size - 6), Math.max(1, size - 6));
        graphics.setColor(text.withAlpha(segment.color, 220));
        graphics.drawOval(x, y, size - 1, size - 1);
        graphics.setColor(Color.WHITE);
        text.drawCenteredText(graphics, metrics, segment.label(), x, y, size, size);
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

    private int orbSize() {
        return Math.max(26, Math.max(config.statIconSize() + 14, config.fontSize() + 12));
    }
}
