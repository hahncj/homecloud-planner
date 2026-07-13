package com.homecloud.planner.roadmap;

import com.homecloud.planner.phase.PhaseService;
import com.homecloud.planner.project.ProjectService;
import com.homecloud.planner.task.TaskFilter;
import com.homecloud.planner.task.TaskService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles the single-request payload the Roadmap UI needs: a project's
 * summary, its ordered phases with progress, and its full task list with
 * computed blocked flags.
 */
@Service
public class RoadmapService {

    private final ProjectService projectService;
    private final PhaseService phaseService;
    private final TaskService taskService;

    public RoadmapService(ProjectService projectService, PhaseService phaseService, TaskService taskService) {
        this.projectService = projectService;
        this.phaseService = phaseService;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getRoadmap(UUID projectId) {
        return new RoadmapResponse(
                projectService.getProject(projectId),
                phaseService.listPhases(projectId),
                taskService.listTasks(projectId, TaskFilter.NONE));
    }
}
