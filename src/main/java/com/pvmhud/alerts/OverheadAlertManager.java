package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

@Singleton
public class OverheadAlertManager {
    private static final int FULL_RESTORE_MIN_SPEC_INCREASE = 30;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private OverheadAlertState state;

    @Inject
    private OverheadMessageRenderer overheadMessageRenderer;

    public void onStatChanged(StatChanged event) {
        if (!state.isBaselineReady()) {
            return;
        }

        String message = null;
        Color color = null;

        if (event.getSkill() == Skill.HITPOINTS) {
            int currentHp = event.getBoostedLevel();

            if (config.overheadHpAlertEnabled()
                    && crossedDown(state.getPreviousHitpoints(), currentHp, config.hpLowThreshold())) {
                message = config.lowHpOverheadMessage();
                color = config.hpLowColor();
            }

            state.setPreviousHitpoints(currentHp);
        } else if (event.getSkill() == Skill.PRAYER) {
            int currentPrayer = event.getBoostedLevel();

            if (config.overheadPrayerAlertEnabled()
                    && crossedDown(state.getPreviousPrayer(), currentPrayer, config.prayerLowThreshold())) {
                message = config.lowPrayerOverheadMessage();
                color = config.prayerLowColor();
            }

            state.setPreviousPrayer(currentPrayer);
        }

        if (message != null) {
            overheadMessageRenderer.showLocalMessage(message, color);
        }
    }

    public void onSpecPercentChanged(int currentSpecPercent) {
        if (!state.isBaselineReady()) {
            return;
        }

        if (!state.isPreviousSpecReady()) {
            state.setPreviousSpec(currentSpecPercent);
            state.setPreviousSpecReady(true);
            return;
        }

        int previousSpec = state.getPreviousSpec();
        int specChange = currentSpecPercent - previousSpec;

        if (config.overheadSpecAlertEnabled()
                && specChange > 0
                && !isLikelyFullRestore(specChange, currentSpecPercent)
                && crossedUp(previousSpec, currentSpecPercent, config.specThreshold())) {
            overheadMessageRenderer.showLocalMessage(config.specOverheadMessage(), config.specHighColor());
        }

        state.setPreviousSpec(currentSpecPercent);
    }

    private static boolean crossedDown(int previousValue, int currentValue, int threshold) {
        return previousValue > threshold && currentValue <= threshold;
    }

    private static boolean crossedUp(int previousValue, int currentValue, int threshold) {
        return previousValue < threshold && currentValue >= threshold;
    }

    private static boolean isLikelyFullRestore(int specChange, int currentSpecPercent) {
        return currentSpecPercent == 100 && specChange >= FULL_RESTORE_MIN_SPEC_INCREASE;
    }
}
