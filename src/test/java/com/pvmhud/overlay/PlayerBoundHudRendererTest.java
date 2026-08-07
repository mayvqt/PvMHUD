package com.pvmhud.overlay;

import org.junit.Test;

import java.awt.Point;

import static org.junit.Assert.assertEquals;

public class PlayerBoundHudRendererTest {
    private static final Point PLAYER = new Point(120, 220);

    @Test
    public void positionsStatsOnEitherSideWithOffsets() {
        assertEquals(new Point(132, 220),
                PlayerBoundHudRenderer.statLocation(PLAYER, 30, PlayerStatSide.RIGHT, 12, -55));
        assertEquals(new Point(78, 220),
                PlayerBoundHudRenderer.statLocation(PLAYER, 30, PlayerStatSide.LEFT, 12, -55));
    }

    @Test
    public void positionsSpellIconsAroundPlayer() {
        assertEquals(new Point(90, 228),
                PlayerBoundHudRenderer.spellLocation(PLAYER, 60, 20, PlayerSpellPosition.BELOW, 0, 8));
        assertEquals(new Point(52, 210),
                PlayerBoundHudRenderer.spellLocation(PLAYER, 60, 20, PlayerSpellPosition.LEFT, 0, 0));
        assertEquals(new Point(128, 210),
                PlayerBoundHudRenderer.spellLocation(PLAYER, 60, 20, PlayerSpellPosition.RIGHT, 0, 0));
    }
}
