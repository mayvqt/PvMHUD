package com.pvmhud.alerts;

import com.pvmhud.PvMHUDConfig;
import com.pvmhud.tracking.CombatBoost;
import com.pvmhud.tracking.CombatBoosts;
import com.pvmhud.tracking.PotionDisplayText;
import com.pvmhud.tracking.TimedPotionEffect;
import com.pvmhud.tracking.TimedPotionTracker;
import com.pvmhud.tracking.TimeConstants;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;

@Singleton
public class OverheadAlertManager {
    // Natural regen is 10%; Death Charge or Surge potion restores can produce larger legitimate increments.
    private static final int MAX_EXPECTED_INCREMENTAL_SPEC_RESTORE = 25;

    @Inject
    private PvMHUDConfig config;

    @Inject
    private OverheadAlertState state;

    @Inject
    private OverheadMessageRenderer overheadMessageRenderer;

    @Inject
    private TimedPotionTracker timedPotionTracker;

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

        handleCombatBoostChanged(event);

        if (message != null) {
            overheadMessageRenderer.showLocalMessage(message, color);
        }
    }

    public void onSpecPercentChanged(int currentSpecPercent, boolean hasRecentCombatContext) {
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
                && isAlertableSpecRestore(specChange, hasRecentCombatContext)
                && crossedUp(previousSpec, currentSpecPercent, config.specThreshold())) {
            overheadMessageRenderer.showLocalMessage(config.specOverheadMessage(), config.specHighColor());
        }

        state.setPreviousSpec(currentSpecPercent);
    }

    public void onTimedPotionTimersChanged() {
        if (!state.isBaselineReady() || !config.overheadTimedPotionAlertEnabled()) {
            return;
        }

        timedPotionTracker.forEachPotion(this::handleTimedPotionChanged);
    }

    private void handleCombatBoostChanged(StatChanged event) {
        if (!CombatBoosts.isTracked(event.getSkill())) {
            return;
        }

        int previousBoost = state.getPreviousCombatBoost(event.getSkill());
        int currentBoost = event.getBoostedLevel() - event.getLevel();
        int peakBoost = Math.max(state.getPeakCombatBoost(event.getSkill()), currentBoost);

        if (currentBoost <= 0) {
            peakBoost = 0;
        }

        int boostThreshold = boostThreshold(peakBoost);
        if (config.overheadCombatBoostAlertEnabled()
                && CombatBoosts.isEnabled(config, event.getSkill())
                && previousBoost != Integer.MIN_VALUE
                && previousBoost > boostThreshold
                && currentBoost <= boostThreshold
                && currentBoost > 0) {
            CombatBoost boost = new CombatBoost(event.getSkill(), event.getBoostedLevel(), event.getLevel(), peakBoost);
            showPotionAlert(
                    PotionDisplayText.formatBoostMessage(config.combatBoostOverheadMessage(), boost, config.combatBoostThresholdPercent()),
                    config.potionWarningColor()
            );
        }

        state.setPreviousCombatBoost(event.getSkill(), currentBoost);
        state.setPeakCombatBoost(event.getSkill(), peakBoost);
    }

    private void handleTimedPotionChanged(TimedPotionEffect effect) {
        boolean active = effect.isActive();
        boolean wasActive = state.wasTimedPotionActive(effect.getType());
        boolean expiringAlerted = state.isTimedPotionExpiringAlerted(effect.getType());

        if (active && effect.isExpiringSoon(config.potionExpiringSoonSeconds()) && !expiringAlerted) {
            showTimedPotionMessage(config.raidPotionExpiringMessage(), effect, config.potionWarningColor());
            state.setTimedPotionExpiringAlerted(effect.getType(), true);
        }

        if (!active && wasActive) {
            showTimedPotionMessage(config.raidPotionExpiredMessage(), effect, config.potionWarningColor());
            state.setTimedPotionExpiringAlerted(effect.getType(), false);
        }

        if (active && !effect.isExpiringSoon(config.potionExpiringSoonSeconds())) {
            state.setTimedPotionExpiringAlerted(effect.getType(), false);
        }

        state.setTimedPotionActive(effect.getType(), active);
    }

    private void showTimedPotionMessage(String template, TimedPotionEffect effect, Color color) {
        showPotionAlert(PotionDisplayText.formatTimedPotionMessage(template, effect), color);
    }

    private void showPotionAlert(String message, Color color) {
        long now = System.nanoTime();
        long cooldownNanos = TimeConstants.secondsToNanos(config.potionAlertCooldownSeconds());
        if (cooldownNanos > 0L
                && state.getLastPotionAlertNanos() > 0L
                && now - state.getLastPotionAlertNanos() < cooldownNanos) {
            return;
        }

        overheadMessageRenderer.showLocalMessage(message, color);
        state.setLastPotionAlertNanos(now);
    }

    private static boolean crossedDown(int previousValue, int currentValue, int threshold) {
        return previousValue > threshold && currentValue <= threshold;
    }

    private static boolean crossedUp(int previousValue, int currentValue, int threshold) {
        return previousValue < threshold && currentValue >= threshold;
    }

    private static boolean isAlertableSpecRestore(int specChange, boolean hasRecentCombatContext) {
        return specChange > 0
                && (specChange <= MAX_EXPECTED_INCREMENTAL_SPEC_RESTORE || hasRecentCombatContext);
    }

    private int boostThreshold(int peakBoost) {
        if (peakBoost <= 0) {
            return 0;
        }

        return Math.max(1, (int) Math.ceil(peakBoost * config.combatBoostThresholdPercent() / 100.0));
    }
}
