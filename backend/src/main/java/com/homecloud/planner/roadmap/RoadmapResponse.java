package com.homecloud.planner.roadmap;

import com.homecloud.planner.phase.PhaseResponse;
import com.homecloud.planner.project.ProjectResponse;
import com.homecloud.planner.task.TaskResponse;
import java.util.List;

public record RoadmapResponse(ProjectResponse project, List<PhaseResponse> phases, List<TaskResponse> tasks) {
}
