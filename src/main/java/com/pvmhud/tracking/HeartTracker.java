package com.pvmhud.tracking;

import net.runelite.api.gameval.VarbitID;

import javax.inject.Singleton;

@Singleton
public class HeartTracker extends CooldownVarbitTracker {
    @Override
    protected int cooldownVarbitId() {
        return VarbitID.IMBUED_HEART_TIMER;
    }
}
