package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;
import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;

public class HudVisualStateManagerTest {
    @Test
    public void cooldownOnlyTrackersUseTheirConfiguredColors() {
        Color corruptionColor = new Color(1, 2, 3);
        Color heartColor = new Color(4, 5, 6);
        PvMHUDConfig config = new PvMHUDConfig() {
            @Override
            public Color corruptionActiveColor() {
                return corruptionColor;
            }

            @Override
            public Color heartActiveColor() {
                return heartColor;
            }
        };

        ThrallTracker thrall = new ThrallTracker();
        VengeanceTracker vengeance = new VengeanceTracker();
        DeathChargeTracker deathCharge = new DeathChargeTracker();
        MarkOfDarknessTracker markOfDarkness = new MarkOfDarknessTracker();
        CorruptionTracker corruption = new CorruptionTracker();
        WardOfArceuusTracker ward = new WardOfArceuusTracker();
        HeartTracker heart = new HeartTracker();
        HudVisualStateManager manager = new HudVisualStateManager(
                config,
                thrall,
                vengeance,
                deathCharge,
                markOfDarkness,
                corruption,
                ward,
                heart
        );
        VisualState cooldown = new VisualState();
        cooldown.ready = false;
        cooldown.cooldown = true;

        assertEquals(corruptionColor, manager.colorFor(corruption, cooldown, 0L));
        assertEquals(heartColor, manager.colorFor(heart, cooldown, 0L));
    }
}
