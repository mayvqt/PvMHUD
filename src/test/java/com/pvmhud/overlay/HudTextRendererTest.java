package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import org.junit.Test;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertTrue;

public class HudTextRendererTest {
    @Test
    public void centeredTextUsesConfiguredOutline() {
        Color outline = new Color(12, 34, 56);
        PvMHUDConfig config = new PvMHUDConfig() {
            @Override
            public boolean textOutline() {
                return true;
            }

            @Override
            public Color outlineColor() {
                return outline;
            }
        };
        HudTextRenderer renderer = new HudTextRenderer(config);

        BufferedImage image = new BufferedImage(100, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            FontMetrics metrics = graphics.getFontMetrics();
            renderer.drawCenteredText(graphics, metrics, "42", 0, 0, 100, 40);
        } finally {
            graphics.dispose();
        }

        boolean foundOutlinePixel = false;
        for (int y = 0; y < image.getHeight() && !foundOutlinePixel; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == outline.getRGB()) {
                    foundOutlinePixel = true;
                    break;
                }
            }
        }

        assertTrue("centered text should use the configured outline", foundOutlinePixel);
    }
}
