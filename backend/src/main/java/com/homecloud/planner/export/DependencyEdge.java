package com.homecloud.planner.export;

import java.util.UUID;

/** A generic "from depends on to" pair, used for both task and service dependencies in an export. */
public record DependencyEdge(UUID from, UUID to) {
}
