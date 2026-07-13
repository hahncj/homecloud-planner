package com.homecloud.planner.decision;

public record ArchitectureDecisionFilter(DecisionStatus status) {

    public static final ArchitectureDecisionFilter NONE = new ArchitectureDecisionFilter(null);
}
