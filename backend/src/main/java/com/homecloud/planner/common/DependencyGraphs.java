package com.homecloud.planner.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shared cycle-detection for "X depends on Y" graphs. Used by both task
 * dependencies and service dependencies, which have identical graph shape
 * (a directed edge set over UUIDs) but belong to different entity types.
 */
public final class DependencyGraphs {

    private DependencyGraphs() {
    }

    public record Edge(UUID from, UUID to) {
    }

    /**
     * Detects whether adding an edge {@code fromId -> toId} (fromId depends
     * on toId) would create a cycle, given the edges already present in the
     * graph.
     */
    public static boolean wouldCreateCycle(UUID fromId, UUID toId, List<Edge> existingEdges) {
        Map<UUID, List<UUID>> adjacency = new HashMap<>();
        for (Edge edge : existingEdges) {
            adjacency.computeIfAbsent(edge.from(), key -> new ArrayList<>()).add(edge.to());
        }

        Deque<UUID> stack = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        stack.push(toId);
        while (!stack.isEmpty()) {
            UUID current = stack.pop();
            if (current.equals(fromId)) {
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
}
