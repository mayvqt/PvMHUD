package com.pvmhud.overlay;

import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PvMHUDPotionOverlay extends AbstractPvMHUDFrameOverlay {
    @Inject
    private PotionSegmentBuilder segmentBuilder;

    public PvMHUDPotionOverlay() {
        super(OverlayPosition.TOP_RIGHT);
    }

    @Override
    protected HudFrame buildFrame(long now) {
        return segmentBuilder.buildTimers(now);
    }

    @Override
    protected HudStyle hudStyle() {
        return config.potionHudStyle().hudStyle();
    }
}
