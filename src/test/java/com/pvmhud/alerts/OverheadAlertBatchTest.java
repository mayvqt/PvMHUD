package com.pvmhud.alerts;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OverheadAlertBatchTest {
    @Test
    public void combinesStatSpecAndSpellAlertsWithIndependentColors() {
        OverheadAlertBatch batch = new OverheadAlertBatch();
        batch.add("Low HP!", Color.RED);
        batch.add("Low Prayer!", Color.BLUE);
        batch.add("Spec!", Color.YELLOW);
        batch.add("Vengeance ended!", Color.GREEN);

        assertEquals("<col=ff0000>Low HP!</col> | <col=0000ff>Low Prayer!</col>"
                + " | <col=ffff00>Spec!</col> | <col=00ff00>Vengeance ended!</col>", batch.drain());
        assertTrue(batch.isEmpty());
        assertEquals("", batch.drain());
    }

    @Test
    public void deduplicatesWithinOneBatchButAllowsTheNextAlert() {
        OverheadAlertBatch batch = new OverheadAlertBatch();
        batch.add(" Spec! ", Color.YELLOW);
        batch.add("Spec!", Color.YELLOW);
        assertEquals("<col=ffff00>Spec!</col>", batch.drain());

        batch.add("Spec!", Color.YELLOW);
        assertEquals("<col=ffff00>Spec!</col>", batch.drain());
    }

    @Test
    public void ignoresBlankAlertsAndDefaultsMissingColorToWhite() {
        OverheadAlertBatch batch = new OverheadAlertBatch();
        batch.add(null, Color.RED);
        batch.add(" \t ", Color.RED);
        assertTrue(batch.isEmpty());

        batch.add(" Low HP! ", null);
        assertEquals("<col=ffffff>Low HP!</col>", batch.drain());
    }

    @Test
    public void resetDiscardsPendingAlerts() {
        OverheadAlertBatch batch = new OverheadAlertBatch();
        batch.add("Low Prayer!", Color.BLUE);
        batch.clear();
        batch.add("Spec!", Color.YELLOW);

        assertEquals("<col=ffff00>Spec!</col>", batch.drain());
    }
}
