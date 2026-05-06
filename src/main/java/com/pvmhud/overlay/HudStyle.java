package com.pvmhud.overlay;

public enum HudStyle {
    TEXT("Text"),
    GAME_ICONS("Game icons"),
    BARS("Bars"),
    CHIPS("Chips"),
    ORBS("Orbs"),
    STACK("Stack");

    private final String displayName;

    HudStyle(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
