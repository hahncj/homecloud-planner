# ADR-0002: Roadmap domain behavior (projects, phases, tasks, dependencies)

## Status

Accepted

## Context

Milestone 2 introduces the core planning domain: projects contain ordered
phases, phases contain tasks, and tasks may depend on other tasks anywhere
in the same project. The domain needs clear, testable rules for ordering,
deletion, blocked-state, and progress so the roadmap UI and future
milestones (budget, dashboard) can rely on consistent numbers without
re-deriving them.

## Decision

- **Phase ordering.** Each phase has an integer `sequence`, unique and
  contiguous per project starting at 1. Creating a phase appends it at the
  end. Deleting a phase re-sequences the remaining phases to close the gap.
  Reordering is an explicit operation (`PUT .../phases/reorder` with the
  full ordered list of phase IDs) rather than an editable field on a single
  phase, so the contiguous invariant can never be left inconsistent.

- **Deletion is restrictive, not cascading.** A project cannot be deleted
  while it has phases; a phase cannot be deleted while it has tasks. Both
  return `409 Conflict`. This is deliberate: cascading a project delete
  would silently destroy phases, tasks, and dependency history, which
  violates the "do not silently cascade destructive deletes" rule. Users
  must delete children first, which is a small amount of friction in
  exchange for never losing data by accident. Task dependency rows are the
  exception — they are pure relationship records with no meaning once
  either task is gone, so they cascade with the task (`ON DELETE CASCADE`
  in `V2__roadmap.sql`).

- **Dependency validation.** `POST /tasks/{id}/dependencies` rejects, in
  order: self-dependency, duplicate edges, dependencies that cross
  projects, and edges that would create a cycle (direct or indirect).
  Dependencies **may** span phases within the same project — phase
  boundaries are an organizational grouping, not a dependency boundary.
  Cycle detection walks the existing project-wide dependency graph
  (`TaskProgressCalculator.wouldCreateCycle`) rather than looking only at
  the two tasks involved, so indirect cycles (A→B→C→A) are caught.

- **Blocked state is calculated, never persisted.** A task is blocked when
  it is still workable (not `COMPLETED` or `CANCELLED`) and at least one of
  its direct dependencies is not yet `COMPLETED` or `CANCELLED`. Storing a
  `blocked` column would require updating every dependent task on every
  status change; computing it on read keeps the graph as the single source
  of truth and makes it impossible for a stored flag to drift from reality.
  A `COMPLETED` or `CANCELLED` task is never reported as blocked,
  regardless of its own dependencies.

- **Progress is calculated, never persisted, and excludes cancelled
  tasks.** Phase and project progress (`taskCount`, `completedCount`,
  `blockedCount`, `progressPercentage`) are computed from the current task
  list on every read. Cancelled tasks are excluded from both the numerator
  and the denominator — a cancelled task was never going to be completed,
  so including it would understate progress on work that is actually
  still on track.

## Alternatives considered

- Persisting `blocked` and progress fields and updating them via triggers
  or application-level cascades on every write. Rejected: more moving
  parts, more places for the stored value to drift from the graph, and no
  performance need at this scale (a personal hybrid-cloud project roadmap
  is at most hundreds of tasks).
- Cascading deletes for projects and phases. Rejected per the "no silent
  destructive cascades" rule; the explicit-restriction approach is
  consistent with how the rest of the backend treats destructive actions.

## Consequences

Every read of a project or phase does a small amount of computation over
its tasks and dependency edges. This is O(tasks + dependencies) per
request, which is negligible at the scale this application targets, and it
guarantees the numbers shown in the UI always reflect the current graph.

## Revisit criteria

Revisit if project sizes grow large enough (many thousands of tasks) that
recomputing progress and blocked state on every request becomes a
measurable latency problem, or if a future milestone needs blocked/progress
history over time (which would require persisting snapshots regardless).
