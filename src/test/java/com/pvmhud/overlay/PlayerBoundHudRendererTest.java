package com.pvmhud.overlay;

import org.junit.Test;

import java.awt.Point;

import static org.junit.Assert.assertEquals;

public class PlayerBoundHudRendererTest {
    private static final Point PLAYER_HEAD = new Point(120, 160);

    @Test
    public void positionsStatsOnEitherSideWithOffsets() {
        assertEquals(new Point(132, 160),
                PlayerBoundHudRenderer.statLocation(PLAYER_HEAD, 30, PlayerStatSide.RIGHT, 12, -55));
        assertEquals(new Point(78, 160),
                PlayerBoundHudRenderer.statLocation(PLAYER_HEAD, 30, PlayerStatSide.LEFT, 12, -55));
    }

    @Test
    public void positionsSpellIconsAroundPlayer() {
        assertEquals(new Point(90, 278),
                PlayerBoundHudRenderer.spellLocation(PLAYER_HEAD, 60, 20, PlayerSpellPosition.BELOW, 0, 8));
        assertEquals(new Point(52, 205),
                PlayerBoundHudRenderer.spellLocation(PLAYER_HEAD, 60, 20, PlayerSpellPosition.LEFT, 0, 0));
        assertEquals(new Point(128, 205),
                PlayerBoundHudRenderer.spellLocation(PLAYER_HEAD, 60, 20, PlayerSpellPosition.RIGHT, 0, 0));
    }
}
