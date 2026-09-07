package com.pvmhud.runtime;

import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PvMHUDRuntimeControllerTest {
    @Test
    public void identifiesOnlySpellAlertVarbits() {
        assertTrue(PvMHUDRuntimeController.isTrackedSpellAlertVarbit(VarbitID.ARCEUUS_RESURRECTION_ACTIVE));
        assertTrue(PvMHUDRuntimeController.isTrackedSpellAlertVarbit(VarbitID.ARCEUUS_DEATH_CHARGE_ACTIVE));
        assertTrue(PvMHUDRuntimeController.isTrackedSpellAlertVarbit(VarbitID.VENGEANCE_REBOUND));
        assertTrue(PvMHUDRuntimeController.isTrackedSpellAlertVarbit(VarbitID.ARCEUUS_CORRUPTION_COOLDOWN));
        assertTrue(PvMHUDRuntimeController.isTrackedSpellAlertVarbit(VarbitID.IMBUED_HEART_TIMER));
        assertFalse(PvMHUDRuntimeController.isTrackedSpellAlertVarbit(VarbitID.POISON_TYPE));
    }
}
