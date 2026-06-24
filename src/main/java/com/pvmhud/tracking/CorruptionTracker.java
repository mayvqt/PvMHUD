package com.pvmhud.tracking;

import net.runelite.api.gameval.VarbitID;

import javax.inject.Singleton;

@Singleton
public class CorruptionTracker extends CooldownVarbitTracker {
    @Override
    protected int cooldownVarbitId() {
        return VarbitID.ARCEUUS_CORRUPTION_COOLDOWN;
    }
}
