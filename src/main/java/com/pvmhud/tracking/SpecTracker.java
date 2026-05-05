package com.pvmhud.tracking;

import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecTracker implements ResettableTracker {
    private static final int SPEC_DIVISOR = 10;

    @Inject
    private Client client;

    public int getSpecPercent() {
        int rawSpec = client.getVarpValue(GameStateIds.SPECIAL_ATTACK_PERCENT);
        return normalizeSpec(rawSpec);
    }

    @Override
    public void reset() {
        // Spec is read directly from the client varp, so there is no cached state to reset.
    }

    private static int normalizeSpec(int rawValue) {
        return Math.max(0, Math.min(100, rawValue / SPEC_DIVISOR));
    }
}