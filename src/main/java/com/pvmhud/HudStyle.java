package com.pvmhud;

public enum HudStyle {
    TEXT("Text"),
    GAME_ICONS("Game icons"),
    BARS("Bars"),
    CHIPS("Chips"),
    ORBS("Orbs"),
    STACK("Stack");

    private final String name;

    HudStyle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
