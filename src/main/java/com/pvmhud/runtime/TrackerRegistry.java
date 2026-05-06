package com.pvmhud.runtime;

import com.pvmhud.tracking.CorruptionTracker;
import com.pvmhud.tracking.DeathChargeTracker;
import com.pvmhud.tracking.HeartTracker;
import com.pvmhud.tracking.HpTracker;
import com.pvmhud.tracking.MarkOfDarknessTracker;
import com.pvmhud.tracking.PrayerTracker;
import com.pvmhud.tracking.ResettableTracker;
import com.pvmhud.tracking.SpecTracker;
import com.pvmhud.tracking.ThrallTracker;
import com.pvmhud.tracking.VengeanceTracker;
import com.pvmhud.tracking.WardOfArceuusTracker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
class TrackerRegistry {
    private final List<ResettableTracker> trackers;

    @Inject
    TrackerRegistry(
            HpTracker hpTracker,
            PrayerTracker prayerTracker,
            SpecTracker specTracker,
            ThrallTracker thrallTracker,
            MarkOfDarknessTracker markOfDarknessTracker,
            WardOfArceuusTracker wardOfArceuusTracker,
            DeathChargeTracker deathChargeTracker,
            VengeanceTracker vengeanceTracker,
            CorruptionTracker corruptionTracker,
            HeartTracker heartTracker
    ) {
        this.trackers = List.of(
                hpTracker,
                prayerTracker,
                specTracker,
                thrallTracker,
                markOfDarknessTracker,
                wardOfArceuusTracker,
                deathChargeTracker,
                vengeanceTracker,
                corruptionTracker,
                heartTracker
        );
    }

    List<Object> eventSubscribers() {
        return List.copyOf(trackers);
    }

    List<ResettableTracker> resettableTrackers() {
        return List.copyOf(trackers);
    }
}
