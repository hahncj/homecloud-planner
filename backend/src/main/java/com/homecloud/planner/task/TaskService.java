package com.homecloud.planner.task;

import com.homecloud.planner.common.ConflictException;
import com.homecloud.planner.common.InvalidRequestException;
import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.phase.PhaseRepository;
import com.homecloud.planner.roadmap.TaskProgressCalculator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final PhaseRepository phaseRepository;
    private final TaskProgressCalculator progressCalculator;

    public TaskService(
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            PhaseRepository phaseRepository,
            TaskProgressCalculator progressCalculator) {
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.phaseRepository = phaseRepository;
        this.progressCalculator = progressCalculator;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(UUID projectId, TaskFilter filter) {
        List<Task> tasks = taskRepository.findByPhaseProjectId(projectId);
        List<TaskDependency> dependencies = dependencyRepository.findByTaskPhaseProjectId(projectId);
        Map<UUID, Boolean> blockedFlags = progressCalculator.computeBlockedFlags(tasks, dependencies);
        Map<UUID, List<UUID>> dependsOnByTask = groupDependsOn(dependencies);

        return tasks.stream()
                .filter(task -> matches(task, blockedFlags, filter))
                .sorted(Comparator.comparing((Task task) -> task.getPhase().getSequence())
                        .thenComparing(Task::getCreatedAt))
                .map(task -> TaskResponse.of(
                        task, blockedFlags.getOrDefault(task.getId(), false),
                        dependsOnByTask.getOrDefault(task.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID taskId) {
        Task task = requireTask(taskId);
        UUID projectId = task.getPhase().getProject().getId();
        List<Task> tasks = taskRepository.findByPhaseProjectId(projectId);
        List<TaskDependency> dependencies = dependencyRepository.findByTaskPhaseProjectId(projectId);
        Map<UUID, Boolean> blockedFlags = progressCalculator.computeBlockedFlags(tasks, dependencies);
        List<UUID> dependsOnTaskIds = dependencyRepository.findByTaskId(taskId).stream()
                .map(dependency -> dependency.getDependsOnTask().getId())
                .toList();
        return TaskResponse.of(task, blockedFlags.getOrDefault(taskId, false), dependsOnTaskIds);
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, UUID phaseId, TaskRequest request) {
        Phase phase = requirePhaseInProject(projectId, phaseId);
        Task task = new Task(
                phase,
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.estimatedCost(),
                request.actualCost(),
                request.targetDate(),
                request.completedDate(),
                request.acceptanceCriteria(),
                request.notes());
        Task saved = taskRepository.save(task);
        return getTask(saved.getId());
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, TaskRequest request) {
        Task task = requireTask(taskId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setEstimatedCost(request.estimatedCost());
        task.setActualCost(request.actualCost());
        task.setTargetDate(request.targetDate());
        task.setCompletedDate(request.completedDate());
        task.setAcceptanceCriteria(request.acceptanceCriteria());
        task.setNotes(request.notes());
        return getTask(taskId);
    }

    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = requireTask(taskId);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDependencyResponse> listDependencies(UUID taskId) {
        requireTask(taskId);
        return dependencyRepository.findByTaskId(taskId).stream()
                .map(TaskDependencyResponse::of)
                .toList();
    }

    @Transactional
    public TaskDependencyResponse addDependency(UUID taskId, TaskDependencyRequest request) {
        UUID dependsOnTaskId = request.dependsOnTaskId();
        if (taskId.equals(dependsOnTaskId)) {
            throw new InvalidRequestException("A task cannot depend on itself.");
        }

        Task task = requireTask(taskId);
        Task dependsOnTask = requireTask(dependsOnTaskId);

        UUID projectId = task.getPhase().getProject().getId();
        UUID dependsOnProjectId = dependsOnTask.getPhase().getProject().getId();
        if (!projectId.equals(dependsOnProjectId)) {
            throw new InvalidRequestException("A task cannot depend on a task from a different project.");
        }

        if (dependencyRepository.existsByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId)) {
            throw new ConflictException("This dependency already exists.");
        }

        List<TaskDependency> existingProjectDependencies = dependencyRepository.findByTaskPhaseProjectId(projectId);
        if (progressCalculator.wouldCreateCycle(taskId, dependsOnTaskId, existingProjectDependencies)) {
            throw new InvalidRequestException("This dependency would create a cycle.");
        }

        TaskDependency saved = dependencyRepository.save(new TaskDependency(task, dependsOnTask));
        return TaskDependencyResponse.of(saved);
    }

    @Transactional
    public void removeDependency(UUID taskId, UUID dependsOnTaskId) {
        requireTask(taskId);
        if (!dependencyRepository.existsByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId)) {
            throw new NotFoundException("Dependency not found.");
        }
        dependencyRepository.deleteByTaskIdAndDependsOnTaskId(taskId, dependsOnTaskId);
    }

    private Task requireTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task " + taskId + " was not found."));
    }

    private Phase requirePhaseInProject(UUID projectId, UUID phaseId) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new NotFoundException("Phase " + phaseId + " was not found."));
        if (!phase.getProject().getId().equals(projectId)) {
            throw new NotFoundException("Phase " + phaseId + " does not belong to project " + projectId + ".");
        }
        return phase;
    }

    private Map<UUID, List<UUID>> groupDependsOn(List<TaskDependency> dependencies) {
        return dependencies.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        dependency -> dependency.getTask().getId(),
                        java.util.stream.Collectors.mapping(
                                dependency -> dependency.getDependsOnTask().getId(),
                                java.util.stream.Collectors.toList())));
    }

    private boolean matches(Task task, Map<UUID, Boolean> blockedFlags, TaskFilter filter) {
        if (filter.phaseId() != null && !task.getPhase().getId().equals(filter.phaseId())) {
            return false;
        }
        if (filter.status() != null && task.getStatus() != filter.status()) {
            return false;
        }
        if (filter.priority() != null && task.getPriority() != filter.priority()) {
            return false;
        }
        if (filter.blocked() != null
                && !filter.blocked().equals(blockedFlags.getOrDefault(task.getId(), false))) {
            return false;
        }
        return true;
    }
}
