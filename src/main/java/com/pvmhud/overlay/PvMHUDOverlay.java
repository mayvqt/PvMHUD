package com.pvmhud.overlay;

import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PvMHUDOverlay extends AbstractPvMHUDFrameOverlay {
    @Inject
    private HudSegmentBuilder segmentBuilder;

    @Inject
    private HudVisualStateManager visualStateManager;

    @Inject
    private HudIconCache iconCache;

    public PvMHUDOverlay() {
        super(OverlayPosition.TOP_LEFT);
    }

    public void reset() {
        visualStateManager.reset();
        segmentBuilder.reset();
    }

    public void updateFrame(long now) {
        segmentBuilder.update(now);
    }

    public void clearCachedResources() {
        iconCache.clear();
    }

    @Override
    protected HudFrame buildFrame(long now) {
        return segmentBuilder.currentFrame();
    }

    @Override
    protected HudStyle hudStyle() {
        return config.hudStyle();
    }
}
