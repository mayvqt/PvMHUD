package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.TimeConstants;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VengeanceDisplayTest {
    private final PvMHUDConfig config = new PvMHUDConfig() {};
    // Drive varbit events directly without polling a live game client.
    private final VengeanceTracker vengeance = new VengeanceTracker() {
        @Override
        protected void sync() {}
    };
    private final HudVisualStateManager manager = new HudVisualStateManager(
            config,
            new ThrallTracker() {
                @Override
                protected void sync() {}
            },
            vengeance,
            new DeathChargeTracker() {
                @Override
                protected void sync() {}
            },
            new MarkOfDarknessTracker(),
            new CorruptionTracker() {
                @Override
                protected void sync() {}
            },
            new WardOfArceuusTracker() {
                @Override
                protected void sync() {}
            },
            new HeartTracker() {
                @Override
                protected void sync() {}
            }
    );
    private final TrackerDisplay display = manager.displays().stream()
            .filter(candidate -> candidate.tracker == vengeance)
            .findFirst().get();
    private final VisualState state = display.state;
    private long now = TimeConstants.secondsToNanos(100);

    @Test
    public void consumedIconPersistsThroughCooldownAndReadyTimeoutThenRecastRestoresRed() {
        manager.update(now);
        assertFalse(manager.shouldRender(state, now, true));

        change(VarbitID.VENGEANCE_TIMELIMIT, 30);
        change(VarbitID.VENGEANCE_REBOUND, 1);
        assertIcon(SpriteID.LunarMagicOn.VENGEANCE);
        assertTrue(manager.shouldRender(state, now, false));

        change(VarbitID.VENGEANCE_REBOUND, 0);
        assertTrue(state.cooldown);
        assertIcon(SpriteID.LunarMagicOn.VENGEANCE_OTHER);
        assertTrue(manager.shouldRender(state, now, false));

        change(VarbitID.VENGEANCE_TIMELIMIT, 0);
        assertTrue(state.ready);
        assertIcon(SpriteID.LunarMagicOn.VENGEANCE_OTHER);
        assertTrue(manager.shouldRender(state, now, true));
        assertFalse(manager.shouldRender(state, now, false));

        now += TimeConstants.secondsToNanos(config.inactiveSpellTimeoutSeconds() + 1);
        manager.update(now);
        assertFalse(manager.shouldRender(state, now, true));

        change(VarbitID.VENGEANCE_TIMELIMIT, 30);
        change(VarbitID.VENGEANCE_REBOUND, 1);
        assertIcon(SpriteID.LunarMagicOn.VENGEANCE);
        assertTrue(manager.shouldRender(state, now, true));
    }

    @Test
    public void endingCooldownDoesNotTurnStoredVengeanceWhite() {
        change(VarbitID.VENGEANCE_TIMELIMIT, 30);
        change(VarbitID.VENGEANCE_REBOUND, 1);
        change(VarbitID.VENGEANCE_TIMELIMIT, 0);
        assertTrue(state.active);
        assertIcon(SpriteID.LunarMagicOn.VENGEANCE);

        change(VarbitID.VENGEANCE_REBOUND, 0);
        assertTrue(state.ready);
        assertIcon(SpriteID.LunarMagicOn.VENGEANCE_OTHER);
        assertTrue(manager.shouldRender(state, now, true));

        vengeance.reset();
        manager.reset();
        manager.update(++now);
        assertFalse(manager.shouldRender(state, now, true));
    }

    private void change(int varbit, int value) {
        VarbitChanged event = new VarbitChanged();
        event.setVarbitId(varbit);
        event.setValue(value);
        vengeance.onVarbitChanged(event);
        now += TimeConstants.secondsToNanos(1);
        manager.update(now);
    }

    private void assertIcon(int spriteId) {
        IconRef icon = manager.iconFor(display);
        assertEquals(IconGroup.SPELL, icon.group);
        assertEquals(spriteId, icon.id);
    }
}
