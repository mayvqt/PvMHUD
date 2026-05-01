package com.pvmhud.tracking;

import javax.inject.Singleton;

@Singleton
public class HeartTracker extends CooldownVarbitTracker {
    @Override
    protected int cooldownVarbitId() {
        return GameStateIds.HEART_COOLDOWN;
    }
}
