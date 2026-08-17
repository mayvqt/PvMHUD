package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.SpellStateTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Singleton
public class SpellExpiryAlertManager {
    private final PvMHUDConfig config;
    private final OverheadMessageRenderer renderer;
    private final List<TrackedState> trackedStates;

    @Inject
    SpellExpiryAlertManager(
            PvMHUDConfig config,
            OverheadMessageRenderer renderer,
            ThrallTracker thrall,
            DeathChargeTracker deathCharge,
            MarkOfDarknessTracker markOfDarkness,
            VengeanceTracker vengeance,
            WardOfArceuusTracker wardOfArceuus,
            CorruptionTracker corruption,
            HeartTracker heart
    ) {
        this.config = config;
        this.renderer = renderer;
        trackedStates = List.of(
                ended(thrall, config::overheadThrallExpiryAlert, config::thrallExpiryOverheadMessage),
                ended(deathCharge, config::overheadDeathChargeExpiryAlert, config::deathChargeExpiryOverheadMessage),
                ended(markOfDarkness, config::overheadMarkOfDarknessExpiryAlert, config::markOfDarknessExpiryOverheadMessage),
                ended(vengeance, config::overheadVengeanceExpiryAlert, config::vengeanceExpiryOverheadMessage),
                ended(wardOfArceuus, config::overheadWardExpiryAlert, config::wardExpiryOverheadMessage),
                ready(corruption, config::overheadCorruptionReadyAlert, config::corruptionReadyOverheadMessage),
                ready(heart, config::overheadHeartReadyAlert, config::heartReadyOverheadMessage)
        );
    }

    public void update() {
        if (!config.overheadSpellExpiryAlerts()) {
            reset();
            return;
        }

        StringJoiner messages = new StringJoiner(" ");
        for (TrackedState trackedState : trackedStates) {
            if (trackedState.update()) {
                String message = trackedState.message.get();
                if (message != null && !message.trim().isEmpty()) {
                    messages.add(message.trim());
                }
            }
        }

        if (messages.length() > 0) {
            renderer.showLocalMessage(messages.toString(), config.spellExpiryOverheadColor());
        }
    }

    public void reset() {
        for (TrackedState trackedState : trackedStates) {
            trackedState.reset();
        }
    }

    private static TrackedState ended(
            SpellStateTracker tracker,
            BooleanSupplier enabled,
            Supplier<String> message
    ) {
        return new TrackedState(enabled, tracker::hasActiveEffect, message);
    }

    private static TrackedState ready(
            SpellStateTracker tracker,
            BooleanSupplier enabled,
            Supplier<String> message
    ) {
        return new TrackedState(enabled, tracker::isOnCooldown, message);
    }

    private static final class TrackedState {
        private final BooleanSupplier enabled;
        private final BooleanSupplier state;
        private final Supplier<String> message;
        private boolean initialized;
        private boolean previous;

        private TrackedState(BooleanSupplier enabled, BooleanSupplier state, Supplier<String> message) {
            this.enabled = enabled;
            this.state = state;
            this.message = message;
        }

        private boolean update() {
            if (!enabled.getAsBoolean()) {
                reset();
                return false;
            }

            boolean current = state.getAsBoolean();
            if (!initialized) {
                initialized = true;
                previous = current;
                return false;
            }

            boolean ended = previous && !current;
            previous = current;
            return ended;
        }

        private void reset() {
            initialized = false;
            previous = false;
        }
    }
}
