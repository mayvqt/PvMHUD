package com.pvmhud.overlay;

public enum PlayerSpellPosition {
    BELOW("Below"),
    LEFT("Left"),
    RIGHT("Right");

    private final String displayName;

    PlayerSpellPosition(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
