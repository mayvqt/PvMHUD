package com.pvmhud.overlay;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.SpellStateTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.TimeConstants;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;
import net.runelite.api.gameval.SpriteID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@Singleton
final class HudVisualStateManager {
    @Inject
    private PvMHUDConfig config;

    @Inject
    private ThrallTracker thrallTracker;

    @Inject
    private VengeanceTracker vengeanceTracker;

    @Inject
    private DeathChargeTracker deathChargeTracker;

    @Inject
    private MarkOfDarknessTracker markOfDarknessTracker;

    @Inject
    private CorruptionTracker corruptionTracker;

    @Inject
    private WardOfArceuusTracker wardOfArceuusTracker;

    @Inject
    private HeartTracker heartTracker;

    private final List<TrackerDisplay> trackerDisplays = new ArrayList<>();
    private final List<VisualState> spellVisualStates = new ArrayList<>();
    private final VisualState heartVisualState = new VisualState();

    List<TrackerDisplay> displays() {
        initialiseDisplays();
        return trackerDisplays;
    }

    VisualState spellState(int index) {
        return spellVisualStates.get(index);
    }

    VisualState heartState() {
        return heartVisualState;
    }

    HeartTracker heartTracker() {
        return heartTracker;
    }

    void update(long now) {
        initialiseDisplays();

        for (int i = 0; i < trackerDisplays.size(); i++) {
            updateVisualState(trackerDisplays.get(i).tracker, spellVisualStates.get(i), now);
        }

        updateVisualState(heartTracker, heartVisualState, now);
    }

    void reset() {
        for (VisualState state : spellVisualStates) {
            state.reset();
        }
        heartVisualState.reset();
    }

    boolean shouldRender(VisualState state, long now, boolean allowInactive) {
        if (!state.ready) {
            return true;
        }

        return allowInactive
                && config.inactiveSpellTimeoutSeconds() > 0
                && state.lastVisibleNanos > 0L
                && now - state.lastVisibleNanos <= TimeConstants.secondsToNanos(config.inactiveSpellTimeoutSeconds());
    }

    Color colorFor(SpellStateTracker tracker, VisualState state, long now) {
        if (state.expiringSoon) {
            return config.expiringSpellColor();
        }

        if (state.active) {
            return activeColor(tracker);
        }

        if (state.cooldown) {
            return tracker == deathChargeTracker ? config.deathChargeCooldownColor() : config.cooldownSpellColor();
        }

        if (shouldFlashReady(state, now)) {
            return config.readySpellFlashColor();
        }

        return config.readySpellColor();
    }

    private void initialiseDisplays() {
        if (!trackerDisplays.isEmpty()) {
            return;
        }

        trackerDisplays.add(new TrackerDisplay(thrallTracker, "T", IconRef.spell(SpriteID.MagicNecroOn.RESURRECT_SUPERIOR_SKELETON), config::showThrall));
        trackerDisplays.add(new TrackerDisplay(deathChargeTracker, "D", IconRef.spell(SpriteID.MagicNecroOn.DEATH_CHARGE), config::showDeathCharge));
        trackerDisplays.add(new TrackerDisplay(markOfDarknessTracker, "M", IconRef.spell(SpriteID.MagicNecroOn.MARK_OF_DARKNESS), config::showMarkOfDarkness));
        trackerDisplays.add(new TrackerDisplay(vengeanceTracker, "V", IconRef.spell(SpriteID.LunarMagicOn.VENGEANCE), config::showVengeance));
        trackerDisplays.add(new TrackerDisplay(corruptionTracker, "C", IconRef.spell(SpriteID.MagicNecroOn.GREATER_CORRUPTION), config::showCorruption));
        trackerDisplays.add(new TrackerDisplay(wardOfArceuusTracker, "W", IconRef.spell(SpriteID.MagicNecroOn.WARD_OF_ARCEUUS), config::showWardOfArceuus));

        for (int i = 0; i < trackerDisplays.size(); i++) {
            spellVisualStates.add(new VisualState());
        }
    }

    private void updateVisualState(SpellStateTracker tracker, VisualState state, long now) {
        boolean active = tracker.isActive();
        boolean cooldown = !active && tracker.isOnCooldown();
        boolean ready = !active && !cooldown;
        boolean expiringSoon = tracker.isExpiringSoon(config.spellExpiringSoonSeconds());

        if (state.initialised) {
            if (state.ready && !ready) {
                state.lastTransitionNanos = now;
            } else if (!state.ready && ready) {
                state.lastTransitionNanos = now;
            }
        } else {
            state.initialised = true;
        }

        if (!ready) {
            state.lastVisibleNanos = now;
        }

        state.active = active;
        state.cooldown = cooldown;
        state.ready = ready;
        state.expiringSoon = expiringSoon;

        if (ready && state.lastTransitionNanos > 0L && config.readySpellFlashRecentSeconds() > 0) {
            long flashWindowNanos = TimeConstants.secondsToNanos(config.readySpellFlashRecentSeconds());
            if (now - state.lastTransitionNanos > flashWindowNanos) {
                state.lastTransitionNanos = 0L;
            }
        }
    }

    private boolean shouldFlashReady(VisualState state, long now) {
        if (!config.flashReadySpells()) {
            return false;
        }

        int windowSeconds = config.readySpellFlashRecentSeconds();
        if (windowSeconds == 0) {
            return isFlashPhase(now);
        }

        return state.lastTransitionNanos > 0L
                && now - state.lastTransitionNanos <= TimeConstants.secondsToNanos(windowSeconds)
                && isFlashPhase(now);
    }

    private boolean isFlashPhase(long now) {
        long period = Math.max(100L, config.flashPeriodMillis()) * TimeConstants.NS_PER_MS;
        return (now / period) % 2L == 0L;
    }

    private Color activeColor(SpellStateTracker tracker) {
        if (tracker == thrallTracker) {
            return config.thrallActiveColor();
        }
        if (tracker == markOfDarknessTracker) {
            return config.markOfDarknessActiveColor();
        }
        if (tracker == deathChargeTracker) {
            return config.deathChargeActiveColor();
        }
        if (tracker == vengeanceTracker) {
            return config.vengeanceActiveColor();
        }
        if (tracker == corruptionTracker) {
            return config.corruptionActiveColor();
        }
        if (tracker == wardOfArceuusTracker) {
            return config.wardOfArceuusActiveColor();
        }
        if (tracker == heartTracker) {
            return config.heartActiveColor();
        }

        return config.readySpellColor();
    }
}
