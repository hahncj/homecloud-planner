package com.homecloud.planner.dashboard;

import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskPriority;
import com.homecloud.planner.task.TaskStatus;
import java.time.LocalDate;
import java.util.UUID;

public record TaskSummary(
        UUID id,
        String title,
        UUID phaseId,
        String phaseName,
        TaskStatus status,
        TaskPriority priority,
        LocalDate targetDate,
        LocalDate completedDate,
        boolean blocked) {

    public static TaskSummary of(Task task, boolean blocked) {
        return new TaskSummary(
                task.getId(),
                task.getTitle(),
                task.getPhase().getId(),
                task.getPhase().getName(),
                task.getStatus(),
                task.getPriority(),
                task.getTargetDate(),
                task.getCompletedDate(),
                blocked);
    }
}
