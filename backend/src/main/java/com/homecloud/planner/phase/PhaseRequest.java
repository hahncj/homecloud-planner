package com.homecloud.planner.phase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PhaseRequest(@NotBlank @Size(max = 200) String name, String description) {
}
