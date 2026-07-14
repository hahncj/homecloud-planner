package com.homecloud.planner.dashboard;

/**
 * A deterministic, rule-based suggestion — never AI-generated. Each
 * category corresponds to one rule in {@link RecommendedActionEngine}.
 */
public record RecommendedAction(String category, String message) {
}
