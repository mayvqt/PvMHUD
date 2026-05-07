package com.pvmhud.overlay;

import javax.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

@Singleton
final class OrbHudRenderer extends AbstractHudRenderer {
    Dimension render(Graphics2D graphics, FontMetrics metrics, HudFrame frame) {
        var spells = frame.spells();
        var hearts = frame.hearts();
        int spellCount = spells.size() + hearts.size();
        int gap = barGap();
        int spellTileSize = config.barSpellTileSize();
        int orb = orbSize();
        int tile = Math.max(14, spellTileSize);

        if (config.verticalLayout()) {
            return renderVertical(graphics, metrics, frame, spells, hearts, gap, orb, tile);
        }

        int statWidth = frame.stats().isEmpty() ? 0 : frame.stats().size() * orb + Math.max(0, frame.stats().size() - 1) * gap;
        int spellWidth = spellCount == 0 ? 0 : spellCount * tile + Math.max(0, spellCount - 1) * gap;
        int width = Math.max(statWidth, spellWidth) + HudConstants.PADDING_X * 2;
        int rows = (frame.stats().isEmpty() ? 0 : 1) + (spellCount == 0 ? 0 : 1);
        int height = (frame.stats().isEmpty() ? 0 : orb) + (spellCount == 0 ? 0 : tile)
                + Math.max(0, rows - 1) * gap + HudConstants.PADDING_Y * 2;

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
            y += tile + gap;
        }

        if (!frame.stats().isEmpty()) {
            int x = centeredStartX(width, statWidth);
            for (Segment segment : frame.stats()) {
                drawStatOrb(graphics, metrics, segment, x, y, orb);
                x += orb + gap;
            }
        }

        return new Dimension(width, height);
    }

    private Dimension renderVertical(Graphics2D graphics, FontMetrics metrics, HudFrame frame, java.util.List<Segment> spells, java.util.List<Segment> hearts, int gap, int orb, int tile) {
        int statHeight = frame.stats().isEmpty() ? 0 : frame.stats().size() * orb + Math.max(0, frame.stats().size() - 1) * gap;
        int spellCount = spells.size() + hearts.size();
        int spellHeight = spellCount == 0 ? 0 : spellCount * tile + Math.max(0, spellCount - 1) * gap;
        int columnGap = !frame.stats().isEmpty() && spellCount > 0 ? gap + 2 : 0;
        int width = (frame.stats().isEmpty() ? 0 : orb) + columnGap + (spellCount == 0 ? 0 : tile) + HudConstants.PADDING_X * 2;
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
        for (Segment segment : hearts) {
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

    private int orbSize() {
        return Math.max(26, Math.max(config.statIconSize() + 14, config.fontSize() + 12));
    }
}
