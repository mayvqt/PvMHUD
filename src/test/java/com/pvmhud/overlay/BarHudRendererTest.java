package com.pvmhud.overlay;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BarHudRendererTest {
    @Test
    public void clampsAndScalesProgressToBarLength() {
        assertEquals(0, BarHudRenderer.filledLength(100, -0.5d));
        assertEquals(25, BarHudRenderer.filledLength(100, 0.25d));
        assertEquals(100, BarHudRenderer.filledLength(100, 1.5d));
    }
}
