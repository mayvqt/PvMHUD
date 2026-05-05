package com.pvmhud.overlay;

import com.pvmhud.HudFont;
import com.pvmhud.PvMHUDConfig;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Font;

@Singleton
final class HudFontResolver {
    @Inject
    private PvMHUDConfig config;

    Font resolve(Font baseFont) {
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
}
