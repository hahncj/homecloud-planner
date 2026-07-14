# ADR-0004: Service dependency behavior

## Status

Accepted

## Context

Milestone 4 adds hardware inventory (`Device`) and a service catalog
(`ManagedService`) so a service can record which device hosts it and which
other services it depends on (e.g. a reverse proxy depends on an auth
provider). Two relationships need rules: a service's link to its host
device, and the service-to-service dependency graph.

## Decision

- **Host device is a soft reference, not a hard dependency.** A service's
  `hostDeviceId` FK is `ON DELETE SET NULL`. Deleting a device that
  currently hosts a service clears the link on that service rather than
  being blocked or cascading the service away — a service outliving the
  record of what used to host it is normal (the workload gets moved or
  reinstalled elsewhere), and blocking a hardware deletion because
  something *used to* point at it would be surprising. This mirrors the
  `PurchaseItem.phaseId` soft-reference pattern from Milestone 3.

- **Service dependency validation mirrors task dependencies**
  ([ADR-0002](ADR-0002-roadmap-domain-behavior.md)): a service cannot
  depend on itself, duplicate edges are rejected, and both direct and
  indirect cycles are rejected. The cycle-detection graph walk was
  extracted into a shared `common.DependencyGraphs` utility so task
  dependencies and service dependencies — two structurally identical
  "X depends on Y" graphs over different entity types — share one tested
  implementation instead of two copies of the same algorithm.

- **Service deletion must not silently remove relationships.** Unlike a
  task (Milestone 2), where cascading a deleted task's own dependency rows
  away is harmless because those rows only describe that task's own
  graph position, a service dependency edge describes *another* service's
  requirements. If service B is deleted while service A still declares
  "A depends on B," silently dropping that edge would leave A's
  configuration lying about what it needs. So: deleting a service is
  rejected (`409 Conflict`) while any other service still depends on it;
  the dependent edge must be removed first (or the dependent service's
  requirement re-pointed) as an explicit, visible action. A service with
  no dependents can always be deleted — deleting it only removes its own
  outgoing dependency rows, which is the same "safe to cascade because
  it's this row's own data" reasoning used for `task_dependency`.

- **No graphical topology editor.** The dependency list and blocked/status
  indicators are sufficient for Milestone 4; a visual graph editor is
  explicitly deferred per the build plan's scope control.

## Alternatives considered

- Cascading service deletes to also delete dependent services. Rejected:
  deleting a shared dependency (e.g. a database) should not silently take
  down every service that relies on it.
- Blocking host-device deletion whenever a service references it, the same
  way phase deletion is blocked by tasks. Rejected: a host device is
  informational metadata on a service, not a structural parent the service
  can't exist without (unlike a task's phase), so the stricter phase/task
  rule doesn't apply here.

## Consequences

Deleting infrastructure that's still relied upon always requires an
explicit, visible step (removing dependency edges first), while deleting
hardware that a service happens to reference is unblocked and simply
clears that reference. Cycle detection logic is shared and only needs to
be correct once.

## Revisit criteria

Revisit if a future milestone wants a "force delete with cascade"
operation for services (e.g. decommissioning a whole stack at once) — that
would need an explicit, separately-confirmed bulk operation rather than a
change to this ADR's single-delete semantics.
