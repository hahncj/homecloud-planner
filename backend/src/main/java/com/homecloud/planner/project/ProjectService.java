package com.homecloud.planner.project;

import com.homecloud.planner.common.ConflictException;
import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.phase.PhaseRepository;
import com.homecloud.planner.roadmap.ProgressSummary;
import com.homecloud.planner.roadmap.TaskProgressCalculator;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskDependency;
import com.homecloud.planner.task.TaskDependencyRepository;
import com.homecloud.planner.task.TaskRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final TaskProgressCalculator progressCalculator;

    public ProjectService(
            ProjectRepository projectRepository,
            PhaseRepository phaseRepository,
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            TaskProgressCalculator progressCalculator) {
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.progressCalculator = progressCalculator;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll().stream()
                .map(project -> ProjectResponse.of(project, progressFor(project.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId) {
        Project project = requireProject(projectId);
        return ProjectResponse.of(project, progressFor(projectId));
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project(
                request.name(),
                request.description(),
                request.status(),
                request.budget(),
                request.startDate(),
                request.targetDate());
        Project saved = projectRepository.save(project);
        return ProjectResponse.of(saved, ProgressSummary.EMPTY);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request) {
        Project project = requireProject(projectId);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(request.status());
        project.setBudget(request.budget());
        project.setStartDate(request.startDate());
        project.setTargetDate(request.targetDate());
        return ProjectResponse.of(project, progressFor(projectId));
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = requireProject(projectId);
        if (phaseRepository.existsByProjectId(projectId)) {
            throw new ConflictException("Project has phases; delete its phases before deleting the project.");
        }
        projectRepository.delete(project);
    }

    private ProgressSummary progressFor(UUID projectId) {
        List<Task> tasks = taskRepository.findByPhaseProjectId(projectId);
        List<TaskDependency> dependencies = dependencyRepository.findByTaskPhaseProjectId(projectId);
        var blockedFlags = progressCalculator.computeBlockedFlags(tasks, dependencies);
        return progressCalculator.summarize(tasks, blockedFlags);
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }
}
