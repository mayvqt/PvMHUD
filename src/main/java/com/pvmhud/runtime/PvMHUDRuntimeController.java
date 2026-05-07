package com.pvmhud.runtime;

import com.pvmhud.alerts.OverheadAlertManager;
import com.pvmhud.alerts.OverheadAlertState;
import com.pvmhud.overlay.PvMHUDOverlay;
import com.pvmhud.tracking.ResettableTracker;
import com.pvmhud.tracking.SpecTracker;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PvMHUDRuntimeController {
    @Inject
    private EventBus eventBus;

    @Inject
    private PvMHUDOverlay hudOverlay;

    @Inject
    private SpecTracker specTracker;

    @Inject
    private TrackerRegistry trackerRegistry;

    @Inject
    private OverheadAlertState overheadAlertState;

    @Inject
    private OverheadAlertManager overheadAlertManager;

    private List<Object> eventSubscribers = List.of();
    private List<ResettableTracker> resettableTrackers = List.of();

    public void start() {
        resettableTrackers = trackerRegistry.trackers();
        eventSubscribers = new ArrayList<Object>(resettableTrackers);
        resetSessionState();

        for (Object subscriber : eventSubscribers) {
            eventBus.register(subscriber);
        }
    }

    public void stop() {
        for (Object subscriber : eventSubscribers) {
            eventBus.unregister(subscriber);
        }

        resetSessionState();
        eventSubscribers = List.of();
        resettableTrackers = List.of();
    }

    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();

        if (state == GameState.LOGGED_IN) {
            overheadAlertState.captureBaseline();
            return;
        }

        if (state == GameState.HOPPING || state == GameState.LOGIN_SCREEN) {
            resetSessionState();
        }
    }

    public void onStatChanged(StatChanged event) {
        overheadAlertManager.onStatChanged(event);
    }

    public void onVarbitChanged(VarbitChanged event) {
        overheadAlertManager.onSpecPercentChanged(specTracker.getSpecPercent());
    }

    private void resetSessionState() {
        hudOverlay.reset();

        for (ResettableTracker tracker : resettableTrackers) {
            tracker.reset();
        }
        overheadAlertState.reset();
    }
}
