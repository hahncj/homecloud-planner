package com.homecloud.planner.dashboard;

import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.roadmap.ProgressSummary;
import java.util.UUID;

public record PhaseSummary(UUID id, String name, int sequence, ProgressSummary progress) {

    public static PhaseSummary of(Phase phase, ProgressSummary progress) {
        return new PhaseSummary(phase.getId(), phase.getName(), phase.getSequence(), progress);
    }
}
