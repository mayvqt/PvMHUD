package com.pvmhud.tracking;

import javax.inject.Singleton;

@Singleton
public class CorruptionTracker extends CooldownVarbitTracker {
    @Override
    protected int cooldownVarbitId() {
        return GameStateIds.CORRUPTION_COOLDOWN;
    }
}
