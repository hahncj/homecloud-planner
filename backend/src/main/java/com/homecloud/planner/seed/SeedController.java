package com.homecloud.planner.seed;

import com.homecloud.planner.project.Project;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Only registered under the "dev" Spring profile (see application-dev.yml /
 * SPRING_PROFILES_ACTIVE) so this endpoint does not exist at all in a
 * default or production deployment — see ADR-0006.
 */
@RestController
@RequestMapping("/api/v1/dev/seed")
@Profile("dev")
class SeedController {

    private final SeedService seedService;

    SeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping
    ResponseEntity<SeedResult> seed() {
        Project project = seedService.reseed();
        return ResponseEntity.status(HttpStatus.CREATED).body(new SeedResult(project.getId(), project.getName()));
    }
}
