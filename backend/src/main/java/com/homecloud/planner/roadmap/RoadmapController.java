package com.homecloud.planner.roadmap;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/roadmap")
class RoadmapController {

    private final RoadmapService roadmapService;

    RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    RoadmapResponse getRoadmap(@PathVariable UUID projectId) {
        return roadmapService.getRoadmap(projectId);
    }
}
