package com.pvmhud.overlay;

import java.util.List;

final class HudFrame {
    static final HudFrame EMPTY = new HudFrame(List.of(), List.of(), List.of());

    private final List<Segment> statSegments;
    private final List<Segment> spellSegments;
    private final List<Segment> heartSegments;

    HudFrame(List<Segment> statSegments, List<Segment> spellSegments, List<Segment> heartSegments) {
        this.statSegments = statSegments;
        this.spellSegments = spellSegments;
        this.heartSegments = heartSegments;
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
