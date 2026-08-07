package com.pvmhud.overlay;

public enum PlayerStatSide {
    LEFT("Left"),
    RIGHT("Right");

    private final String displayName;

    PlayerStatSide(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
