package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import org.junit.Test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextIconHudRendererTest {
    @Test
    public void textStyleIgnoresIconSizesInBothLayouts() {
        HudFrame frame = new HudFrame(
                List.of(new Segment(SegmentKind.STAT, "H 99", "99", Color.RED, null)),
                List.of(new Segment(SegmentKind.SPELL, "V", "", Color.BLUE, null)),
                List.of(new Segment(SegmentKind.HEART, "Heart", "", Color.GREEN, null))
        );

        for (boolean vertical : new boolean[]{false, true}) {
            PvMHUDConfig config = new PvMHUDConfig() {
                @Override
                public boolean verticalLayout() {
                    return vertical;
                }

                @Override
                public boolean showIcons() {
                    return true;
                }

                @Override
                public int statIconSize() {
                    return 32;
                }

                @Override
                public int spellIconSize() {
                    return 32;
                }
            };
            TextIconHudRenderer renderer = new TextIconHudRenderer();
            renderer.config = config;
            renderer.text = new HudTextRenderer(config);

            Graphics2D graphics = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
            try {
                FontMetrics metrics = graphics.getFontMetrics();
                Dimension bounds = renderer.render(graphics, metrics, frame, false);
                int expectedHeight = 3 * metrics.getHeight() + 2 * config.rowGap() + 2 * HudConstants.PADDING_Y;
                assertEquals("Text style must not reserve space for invisible icons", expectedHeight, bounds.height);
            } finally {
                graphics.dispose();
            }
        }
    }

    @Test
    public void horizontalHeartRemainsInsideBoundsWhenStatsAreHidden() {
        PvMHUDConfig config = new PvMHUDConfig() {
            @Override
            public boolean verticalLayout() {
                return false;
            }

            @Override
            public boolean showIcons() {
                return false;
            }

            @Override
            public int rowGap() {
                return 3;
            }

            @Override
            public boolean textOutline() {
                return false;
            }

            @Override
            public boolean textShadow() {
                return false;
            }
        };

        TextIconHudRenderer renderer = new TextIconHudRenderer();
        renderer.config = config;
        renderer.text = new HudTextRenderer(config);

        HudFrame frame = new HudFrame(
                List.of(),
                List.of(new Segment(SegmentKind.SPELL, "Spell", "", Color.BLUE, null)),
                List.of(new Segment(SegmentKind.HEART, "Heart", "", Color.RED, null))
        );

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        Dimension bounds;
        try {
            FontMetrics metrics = graphics.getFontMetrics();
            bounds = renderer.render(graphics, metrics, frame, false);
        } finally {
            graphics.dispose();
        }

        int lastPaintedRow = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    lastPaintedRow = y;
                }
            }
        }

        assertTrue(lastPaintedRow >= 0);
        assertTrue("painted content must fit the returned overlay bounds", lastPaintedRow < bounds.height);
    }
}
