package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

@Singleton
public class OverheadAlertManager {
    // Natural regen is 10%; Death Charge and other effects can produce larger legitimate increments.
    private static final int MAX_EXPECTED_INCREMENTAL_SPEC_RESTORE = 25;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private OverheadAlertState state;

    @Inject
    private OverheadMessageRenderer overheadMessageRenderer;

    public void onStatChanged(StatChanged event) {
        if (state.needsBaseline()) {
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

    public void onSpecPercentChanged(int currentSpecPercent, boolean hasRecentCombatContext) {
        if (state.needsBaseline()) {
            return;
        }

        int previousSpec = state.getPreviousSpec();
        int specChange = currentSpecPercent - previousSpec;

        if (config.overheadSpecAlertEnabled()
                && isAlertableSpecRestore(specChange, hasRecentCombatContext)
                && crossedUp(previousSpec, currentSpecPercent, config.specThreshold())) {
            overheadMessageRenderer.showLocalMessage(config.specOverheadMessage(), config.specHighColor());
        }

        state.setPreviousSpec(currentSpecPercent);
    }

    static boolean crossedDown(int previousValue, int currentValue, int threshold) {
        return previousValue > threshold && currentValue <= threshold;
    }

    static boolean crossedUp(int previousValue, int currentValue, int threshold) {
        return previousValue < threshold && currentValue >= threshold;
    }

    static boolean isAlertableSpecRestore(int specChange, boolean hasRecentCombatContext) {
        return specChange > 0
                && (specChange <= MAX_EXPECTED_INCREMENTAL_SPEC_RESTORE || hasRecentCombatContext);
    }

}
