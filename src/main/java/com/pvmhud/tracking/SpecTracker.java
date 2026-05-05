package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecTracker implements ResettableTracker {
    private static final int SPEC_DIVISOR = 10;

    @Inject
    private Client client;

    private int specPercent = -1;

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == GameStateIds.SPECIAL_ATTACK_PERCENT) {
            specPercent = normalizeSpec(event.getValue());
        }
    }

    public int getSpecPercent() {
        if (specPercent < 0) {
            specPercent = normalizeSpec(client.getVarpValue(GameStateIds.SPECIAL_ATTACK_PERCENT));
        }
        return specPercent;
    }

    @Override
    public void reset() {
        specPercent = -1;
    }

    private static int normalizeSpec(int rawValue) {
        int percent = rawValue / SPEC_DIVISOR;
        return Math.max(0, Math.min(100, percent));
    }
}
