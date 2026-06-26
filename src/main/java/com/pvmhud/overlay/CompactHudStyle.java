package com.pvmhud.overlay;

public enum CompactHudStyle {
    GAME_ICONS("Game icons", HudStyle.GAME_ICONS),
    CHIPS("Chips", HudStyle.CHIPS);

    private final String displayName;
    private final HudStyle hudStyle;

    CompactHudStyle(String displayName, HudStyle hudStyle) {
        this.displayName = displayName;
        this.hudStyle = hudStyle;
    }

    HudStyle hudStyle() {
        return hudStyle;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
