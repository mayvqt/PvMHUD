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
import java.util.List;

@Singleton
final class HudVisualStateManager {
    private static final IconRef INACTIVE_VENGEANCE_ICON = IconRef.spell(SpriteID.LunarMagicOn.VENGEANCE_OTHER);

    private final PvMHUDConfig config;
    private final ThrallTracker thrallTracker;
    private final VengeanceTracker vengeanceTracker;
    private final DeathChargeTracker deathChargeTracker;
    private final MarkOfDarknessTracker markOfDarknessTracker;
    private final CorruptionTracker corruptionTracker;
    private final WardOfArceuusTracker wardOfArceuusTracker;
    private final HeartTracker heartTracker;

    private final List<TrackerDisplay> trackerDisplays;
    private final VisualState heartVisualState = new VisualState();

    @Inject
    HudVisualStateManager(
            PvMHUDConfig config,
            ThrallTracker thrallTracker,
            VengeanceTracker vengeanceTracker,
            DeathChargeTracker deathChargeTracker,
            MarkOfDarknessTracker markOfDarknessTracker,
            CorruptionTracker corruptionTracker,
            WardOfArceuusTracker wardOfArceuusTracker,
            HeartTracker heartTracker
    ) {
        this.config = config;
        this.thrallTracker = thrallTracker;
        this.vengeanceTracker = vengeanceTracker;
        this.deathChargeTracker = deathChargeTracker;
        this.markOfDarknessTracker = markOfDarknessTracker;
        this.corruptionTracker = corruptionTracker;
        this.wardOfArceuusTracker = wardOfArceuusTracker;
        this.heartTracker = heartTracker;

        trackerDisplays = List.of(
                new TrackerDisplay(thrallTracker, "T", IconRef.spell(SpriteID.MagicNecroOn.RESURRECT_SUPERIOR_SKELETON), config::showThrall),
                new TrackerDisplay(deathChargeTracker, "D", IconRef.spell(SpriteID.MagicNecroOn.DEATH_CHARGE), config::showDeathCharge),
                new TrackerDisplay(markOfDarknessTracker, "M", IconRef.spell(SpriteID.MagicNecroOn.MARK_OF_DARKNESS), config::showMarkOfDarkness),
                new TrackerDisplay(vengeanceTracker, "V", IconRef.spell(SpriteID.LunarMagicOn.VENGEANCE), config::showVengeance),
                new TrackerDisplay(corruptionTracker, "C", IconRef.spell(SpriteID.MagicNecroOn.GREATER_CORRUPTION), config::showCorruption),
                new TrackerDisplay(wardOfArceuusTracker, "W", IconRef.spell(SpriteID.MagicNecroOn.WARD_OF_ARCEUUS), config::showWardOfArceuus)
        );
    }

    List<TrackerDisplay> displays() {
        return trackerDisplays;
    }

    VisualState heartState() {
        return heartVisualState;
    }

    HeartTracker heartTracker() {
        return heartTracker;
    }

    void update(long now) {
        for (TrackerDisplay display : trackerDisplays) {
            updateVisualState(display.tracker, display.state, now);
        }

        updateVisualState(heartTracker, heartVisualState, now);
    }

    void reset() {
        for (TrackerDisplay display : trackerDisplays) {
            display.state.reset();
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
            if (tracker == deathChargeTracker) {
                return config.deathChargeCooldownColor();
            }
            if (tracker == corruptionTracker || tracker == heartTracker) {
                return activeColor(tracker);
            }
            return config.cooldownSpellColor();
        }

        if (shouldFlashReady(state, now)) {
            return config.readySpellFlashColor();
        }

        return config.readySpellColor();
    }

    IconRef iconFor(TrackerDisplay display) {
        if (display.tracker == vengeanceTracker && !display.state.active) {
            return INACTIVE_VENGEANCE_ICON;
        }
        return display.icon;
    }

    private void updateVisualState(SpellStateTracker tracker, VisualState state, long now) {
        boolean active = tracker.isActive();
        boolean cooldown = !active && tracker.isOnCooldown();
        boolean ready = !active && !cooldown;
        boolean expiringSoon = tracker.isExpiringSoon(config.spellExpiringSoonSeconds());

        if (state.initialised && state.ready != ready) {
            state.lastTransitionNanos = now;
        }
        state.initialised = true;

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
