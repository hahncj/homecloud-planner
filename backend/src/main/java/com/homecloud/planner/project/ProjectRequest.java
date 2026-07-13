package com.homecloud.planner.project;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        @NotNull ProjectStatus status,
        @DecimalMin(value = "0", message = "budget must not be negative") BigDecimal budget,
        LocalDate startDate,
        LocalDate targetDate) {
}
