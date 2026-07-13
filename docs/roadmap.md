# Project Roadmap Status

Tracks where HomeCloud Planner itself stands against the milestones in
`BUILD_PLAN.md`. See `TASKS.md` for the task-level checklist.

| Milestone | Status | Notes |
|---|---|---|
| 1. Repository and development environment | Done | PostgreSQL, Spring Boot, React, Flyway, Docker Compose, CI, health endpoint. |
| 2. Projects, phases, and tasks | Done | Projects, ordered phases, tasks, dependencies (cycle-safe), computed blocked state, computed progress, Roadmap UI (board/table, filters). See [ADR-0002](decisions/ADR-0002-roadmap-domain-behavior.md). |
| 3. Shopping and budget | Done | Purchase items, computed totals, budget summary (committed spending, remaining budget), category summaries, warranty indicators. See [ADR-0003](decisions/ADR-0003-budget-calculation-behavior.md). |
| 4. Hardware inventory and service catalog | Done | Device inventory (cards/table/detail, validated IP/MAC, warranty and lifecycle indicators); ManagedService catalog with dependency graph, cycle detection, and delete-while-depended-upon protection. See [ADR-0004](decisions/ADR-0004-service-dependency-behavior.md). |
| 5. Backup matrix and architecture decisions | Done | Backup policies with computed coverage/warnings (missing local/off-site/encryption, verification overdue) and an explicit RAID/snapshot disclaimer; ADR-style architecture decisions with related devices/services. See [ADR-0005](decisions/ADR-0005-backup-coverage-rules.md). |
| 6. Dashboard and export | Not started | |
| 7. Authentication, testing, and deployment hardening | Not started | |
