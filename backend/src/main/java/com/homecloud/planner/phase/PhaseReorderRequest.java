package com.homecloud.planner.phase;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PhaseReorderRequest(@NotEmpty List<UUID> orderedPhaseIds) {
}
