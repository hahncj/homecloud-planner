package com.homecloud.planner.task;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull TaskStatus status,
        @NotNull TaskPriority priority,
        @DecimalMin(value = "0", message = "estimatedCost must not be negative") BigDecimal estimatedCost,
        @DecimalMin(value = "0", message = "actualCost must not be negative") BigDecimal actualCost,
        LocalDate targetDate,
        LocalDate completedDate,
        String acceptanceCriteria,
        String notes) {
}
