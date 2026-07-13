package com.homecloud.planner.roadmap;

import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskDependency;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Computes blocked state and progress summaries on the fly rather than
 * persisting them, so they always reflect the current task graph.
 */
@Component
public class TaskProgressCalculator {

    /**
     * A task is blocked when it is still workable (not completed or cancelled)
     * and at least one of its direct dependencies is not yet completed or
     * cancelled. Dependencies may live in other phases of the same project, so
     * callers must pass every dependency edge for the whole project.
     */
    public Map<UUID, Boolean> computeBlockedFlags(List<Task> tasks, List<TaskDependency> dependencies) {
        Map<UUID, Boolean> unresolvedById = new HashMap<>();
        for (Task task : tasks) {
            unresolvedById.put(task.getId(), task.isBlockable());
        }

        Map<UUID, List<UUID>> dependsOn = new HashMap<>();
        for (TaskDependency dependency : dependencies) {
            dependsOn.computeIfAbsent(dependency.getTask().getId(), key -> new java.util.ArrayList<>())
                    .add(dependency.getDependsOnTask().getId());
        }

        Map<UUID, Boolean> blocked = new HashMap<>();
        for (Task task : tasks) {
            if (!task.isBlockable()) {
                blocked.put(task.getId(), false);
                continue;
            }
            boolean hasUnresolvedDependency = dependsOn.getOrDefault(task.getId(), List.of()).stream()
                    .anyMatch(dependsOnTaskId -> Boolean.TRUE.equals(unresolvedById.get(dependsOnTaskId)));
            blocked.put(task.getId(), hasUnresolvedDependency);
        }
        return blocked;
    }

    /**
     * Detects whether adding an edge {@code fromTaskId -> toTaskId} (fromTaskId
     * depends on toTaskId) would create a cycle, given the dependency edges
     * already present in the project.
     */
    public boolean wouldCreateCycle(UUID fromTaskId, UUID toTaskId, List<TaskDependency> existingProjectDependencies) {
        Map<UUID, List<UUID>> adjacency = new HashMap<>();
        for (TaskDependency dependency : existingProjectDependencies) {
            adjacency.computeIfAbsent(dependency.getTask().getId(), key -> new java.util.ArrayList<>())
                    .add(dependency.getDependsOnTask().getId());
        }

        Deque<UUID> stack = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        stack.push(toTaskId);
        while (!stack.isEmpty()) {
            UUID current = stack.pop();
            if (current.equals(fromTaskId)) {
                return true;
            }
            if (!visited.add(current)) {
                continue;
            }
            for (UUID next : adjacency.getOrDefault(current, List.of())) {
                stack.push(next);
            }
        }
        return false;
    }

    /**
     * Cancelled tasks are excluded from both the numerator and denominator of
     * progress percentage.
     */
    public ProgressSummary summarize(List<Task> tasks, Map<UUID, Boolean> blockedFlags) {
        List<Task> eligible = tasks.stream().filter(Task::isEligibleForProgress).toList();
        long taskCount = eligible.size();
        long completedCount = eligible.stream()
                .filter(task -> task.getStatus() == com.homecloud.planner.task.TaskStatus.COMPLETED)
                .count();
        long blockedCount = eligible.stream()
                .filter(task -> Boolean.TRUE.equals(blockedFlags.get(task.getId())))
                .count();
        int progressPercentage = taskCount == 0 ? 0 : Math.round(completedCount * 100.0f / taskCount);
        return new ProgressSummary(taskCount, completedCount, blockedCount, progressPercentage);
    }
}
