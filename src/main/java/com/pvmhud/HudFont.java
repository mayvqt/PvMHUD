package com.pvmhud;

public enum HudFont {
    SYSTEM("System"),
    RUNESCAPE("RuneScape"),
    RUNESCAPE_BOLD("RuneScape Bold"),
    RUNESCAPE_SMALL("RuneScape Small");

    private final String displayName;

    HudFont(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
