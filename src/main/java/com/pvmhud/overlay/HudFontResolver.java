package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Font;

@Singleton
final class HudFontResolver {
    @Inject
    private PvMHUDConfig config;

    private Font cachedBaseFont;
    private HudFont cachedType;
    private int cachedSize;
    private boolean cachedBold;
    private Font cachedFont;

    Font resolve(Font baseFont) {
        HudFont type = config.fontType();
        int size = config.fontSize();
        boolean bold = config.boldFont();
        if (cachedFont != null && cachedBaseFont.equals(baseFont) && cachedType == type
                && cachedSize == size && cachedBold == bold) {
            return cachedFont;
        }

        Font resolved;
        switch (type) {
            case RUNESCAPE:
                resolved = FontManager.getRunescapeFont().deriveFont((float) size);
                break;
            case RUNESCAPE_BOLD:
                resolved = FontManager.getRunescapeBoldFont().deriveFont((float) size);
                break;
            case RUNESCAPE_SMALL:
                resolved = FontManager.getRunescapeSmallFont().deriveFont((float) size);
                break;
            case SYSTEM:
            default:
                resolved = baseFont.deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) size);
                break;
        }

        cachedBaseFont = baseFont;
        cachedType = type;
        cachedSize = size;
        cachedBold = bold;
        cachedFont = resolved;
        return resolved;
    }
}
