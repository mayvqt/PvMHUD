package com.pvmhud.overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class HudFrame {
    private final List<Segment> statSegments;
    private final List<Segment> spellSegments;
    private final List<Segment> heartSegments;

    HudFrame(List<Segment> statSegments, List<Segment> spellSegments, List<Segment> heartSegments) {
        this.statSegments = Collections.unmodifiableList(new ArrayList<>(statSegments));
        this.spellSegments = Collections.unmodifiableList(new ArrayList<>(spellSegments));
        this.heartSegments = Collections.unmodifiableList(new ArrayList<>(heartSegments));
    }

    List<Segment> stats() {
        return statSegments;
    }

    List<Segment> spells() {
        return spellSegments;
    }

    List<Segment> hearts() {
        return heartSegments;
    }

    boolean isEmpty() {
        return statSegments.isEmpty() && spellSegments.isEmpty() && heartSegments.isEmpty();
    }
}
