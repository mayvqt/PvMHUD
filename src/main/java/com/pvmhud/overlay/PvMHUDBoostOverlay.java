package com.pvmhud.overlay;

import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PvMHUDBoostOverlay extends AbstractPvMHUDFrameOverlay {
    @Inject
    private PotionSegmentBuilder segmentBuilder;

    public PvMHUDBoostOverlay() {
        super(OverlayPosition.BOTTOM_RIGHT);
    }

    @Override
    protected HudFrame buildFrame(long now) {
        return segmentBuilder.buildBoosts(now);
    }

    @Override
    protected HudStyle hudStyle() {
        return config.boostHudStyle().hudStyle();
    }
}
