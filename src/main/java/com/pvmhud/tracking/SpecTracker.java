package com.pvmhud.tracking;

import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecTracker implements ResettableTracker {
    private static final int SPEC_DIVISOR = 10;

    @Inject
    private Client client;

    public int getSpecPercent() {
        int rawSpec = client.getVarpValue(VarPlayerID.SA_ENERGY);
        return normalizeSpec(rawSpec);
    }

    @Override
    public void reset() {
        // Spec is read directly from the client varp, so there is no cached state to reset.
    }

    static int normalizeSpec(int rawValue) {
        return Math.max(0, Math.min(100, rawValue / SPEC_DIVISOR));
    }
}
