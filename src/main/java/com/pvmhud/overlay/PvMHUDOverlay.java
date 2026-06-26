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
    private HudStyleRenderer styleRenderer;

    @Inject
    private HudIconCache iconCache;

    public PvMHUDOverlay() {
        super(OverlayPosition.TOP_LEFT);
    }

    public void reset() {
        visualStateManager.reset();
    }

    public void clearCachedResources() {
        iconCache.clear();
    }

    @Override
    protected HudFrame buildFrame(long now) {
        return segmentBuilder.build(now);
    }

    @Override
    protected HudStyle hudStyle() {
        return config.hudStyle();
    }
}
