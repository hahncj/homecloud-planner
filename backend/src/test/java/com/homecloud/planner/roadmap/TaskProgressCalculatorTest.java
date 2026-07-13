package com.homecloud.planner.roadmap;

import static org.assertj.core.api.Assertions.assertThat;

import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectStatus;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskDependency;
import com.homecloud.planner.task.TaskPriority;
import com.homecloud.planner.task.TaskStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TaskProgressCalculatorTest {

    private final TaskProgressCalculator calculator = new TaskProgressCalculator();

    private static Project project() {
        Project project = new Project("Personal Hybrid Cloud", null, ProjectStatus.IN_PROGRESS, null, null, null);
        ReflectionTestUtils.setField(project, "id", UUID.randomUUID());
        return project;
    }

    private static Phase phase(Project project, int sequence) {
        Phase phase = new Phase(project, "Phase " + sequence, null, sequence);
        ReflectionTestUtils.setField(phase, "id", UUID.randomUUID());
        return phase;
    }

    private static Task task(Phase phase, TaskStatus status) {
        Task task = new Task(phase, "Task", null, status, TaskPriority.MEDIUM, null, null, null, null, null, null);
        ReflectionTestUtils.setField(task, "id", UUID.randomUUID());
        return task;
    }

    private static TaskDependency dependency(Task task, Task dependsOn) {
        return new TaskDependency(task, dependsOn);
    }

    @Test
    void taskWithIncompleteDependencyIsBlocked() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task a = task(phase, TaskStatus.NOT_STARTED);
        Task b = task(phase, TaskStatus.NOT_STARTED);

        Map<UUID, Boolean> blocked = calculator.computeBlockedFlags(List.of(a, b), List.of(dependency(a, b)));

        assertThat(blocked.get(a.getId())).isTrue();
        assertThat(blocked.get(b.getId())).isFalse();
    }

    @Test
    void taskIsUnblockedOnceItsDependencyIsCompleted() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task a = task(phase, TaskStatus.NOT_STARTED);
        Task b = task(phase, TaskStatus.COMPLETED);

        Map<UUID, Boolean> blocked = calculator.computeBlockedFlags(List.of(a, b), List.of(dependency(a, b)));

        assertThat(blocked.get(a.getId())).isFalse();
    }

    @Test
    void taskDependingOnCancelledTaskIsNotBlocked() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task a = task(phase, TaskStatus.NOT_STARTED);
        Task cancelled = task(phase, TaskStatus.CANCELLED);

        Map<UUID, Boolean> blocked = calculator.computeBlockedFlags(List.of(a, cancelled), List.of(dependency(a, cancelled)));

        assertThat(blocked.get(a.getId())).isFalse();
    }

    @Test
    void completedAndCancelledTasksAreNeverReportedAsBlocked() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task blocker = task(phase, TaskStatus.NOT_STARTED);
        Task completed = task(phase, TaskStatus.COMPLETED);
        Task cancelled = task(phase, TaskStatus.CANCELLED);

        Map<UUID, Boolean> blocked = calculator.computeBlockedFlags(
                List.of(blocker, completed, cancelled),
                List.of(dependency(completed, blocker), dependency(cancelled, blocker)));

        assertThat(blocked.get(completed.getId())).isFalse();
        assertThat(blocked.get(cancelled.getId())).isFalse();
    }

    @Test
    void directCycleIsDetected() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task a = task(phase, TaskStatus.NOT_STARTED);
        Task b = task(phase, TaskStatus.NOT_STARTED);
        // a already depends on b; adding b -> a would be a direct cycle.
        List<TaskDependency> existing = List.of(dependency(a, b));

        assertThat(calculator.wouldCreateCycle(b.getId(), a.getId(), existing)).isTrue();
    }

    @Test
    void indirectCycleIsDetected() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task a = task(phase, TaskStatus.NOT_STARTED);
        Task b = task(phase, TaskStatus.NOT_STARTED);
        Task c = task(phase, TaskStatus.NOT_STARTED);
        // a -> b -> c already exists; adding c -> a would close the loop.
        List<TaskDependency> existing = List.of(dependency(a, b), dependency(b, c));

        assertThat(calculator.wouldCreateCycle(c.getId(), a.getId(), existing)).isTrue();
    }

    @Test
    void nonCyclicDependencyIsAllowed() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task a = task(phase, TaskStatus.NOT_STARTED);
        Task b = task(phase, TaskStatus.NOT_STARTED);
        Task c = task(phase, TaskStatus.NOT_STARTED);
        List<TaskDependency> existing = List.of(dependency(a, b));

        assertThat(calculator.wouldCreateCycle(a.getId(), c.getId(), existing)).isFalse();
    }

    @Test
    void cancelledTasksAreExcludedFromProgressNumeratorAndDenominator() {
        Project project = project();
        Phase phase = phase(project, 1);
        Task completed = task(phase, TaskStatus.COMPLETED);
        Task cancelled = task(phase, TaskStatus.CANCELLED);
        Task notStarted = task(phase, TaskStatus.NOT_STARTED);
        List<Task> tasks = List.of(completed, cancelled, notStarted);
        Map<UUID, Boolean> blocked = calculator.computeBlockedFlags(tasks, List.of());

        ProgressSummary summary = calculator.summarize(tasks, blocked);

        assertThat(summary.taskCount()).isEqualTo(2);
        assertThat(summary.completedCount()).isEqualTo(1);
        assertThat(summary.progressPercentage()).isEqualTo(50);
    }

    @Test
    void emptyTaskListProducesZeroPercentProgressWithoutDivideByZero() {
        ProgressSummary summary = calculator.summarize(List.of(), Map.of());

        assertThat(summary).isEqualTo(ProgressSummary.EMPTY);
    }
}
