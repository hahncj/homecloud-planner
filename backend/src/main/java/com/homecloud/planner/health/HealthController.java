package com.homecloud.planner.health;

import java.time.Clock;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class HealthController {

    private final Clock clock;

    HealthController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/health")
    HealthResponse health() {
        return new HealthResponse("UP", Instant.now(clock));
    }
}
