# ADR-0006: Seed-data strategy

## Status

Accepted

## Context

Milestone 6 asks for a development-only, repeatable seed mechanism that
populates a representative "Personal Hybrid Cloud" project — the six
named phases, and representative tasks, shopping items, devices, managed
services, backup policies, and architecture decisions "based on
repository documentation." The seed must not run automatically in
production.

## Decision

- **The seed content is drawn from the sibling `personal-cloud-docs`
  repository**, not invented: phase names and their contents come from
  `roadmap.md`, services from `service-catalog.md`, network equipment
  from `network-design.md`, the backup matrix from `backup-strategy.md`,
  shopping items from `shopping-list.md`, and one architecture decision
  is a direct restatement of `decisions/ADR-0001-storage-compute-
  separation.md`. This makes the seed genuinely representative of the
  project this application is for, rather than generic placeholder data,
  and it deliberately seeds *realistic gaps* (e.g. the MacBook backup
  policy has no off-site location, several backup policies have never
  been verified) so the backup matrix warnings and dashboard recommended
  actions have real, meaningful examples to display rather than an
  all-green demo.

- **Gated by the Spring `dev` profile, not a property flag or an
  always-registered endpoint with a runtime guard.** `SeedService` and
  `SeedController` both carry `@Profile("dev")`, so outside that profile
  the beans — and therefore the `/api/v1/dev/seed` route — do not exist
  in the application context at all. A property-flag approach (e.g. "if
  `app.seed.enabled=true`") would still register the endpoint and rely on
  a runtime check never being misconfigured; profile-gating removes the
  failure mode entirely; there is no code path in a default or production
  deployment that can reach the seed logic.

- **Repeatable via delete-and-recreate, not upsert.** Calling
  `POST /api/v1/dev/seed` again looks up any existing project named
  "Personal Hybrid Cloud", deletes it and everything under it (in the
  dependency order the restrictive-delete rules require — tasks before
  phases, everything before the project itself — see
  [ADR-0002](ADR-0002-roadmap-domain-behavior.md) and
  [ADR-0004](ADR-0004-service-dependency-behavior.md)), and rebuilds it
  from scratch. This is simpler and more predictable than trying to diff
  and upsert a fixed dataset, and it means a developer can always get
  back to a known-clean demo state with one request.

## Alternatives considered

- A `CommandLineRunner` that seeds automatically on startup under the
  `dev` profile. Rejected: seeding automatically on every restart means
  losing any manual changes made during a dev session, and it isn't
  "repeatable" in the sense of an explicit, on-demand action — an
  explicit `POST` endpoint is repeatable by construction and never
  surprises anyone by silently wiping local edits on a routine restart.
- A property flag instead of a Spring profile. Rejected per the
  profile-gating rationale above — a profile is the stronger guarantee
  that "must not run in production" actually holds.

## Consequences

Running the seed twice in a row is always safe and always produces the
same six phases with the same representative content; there is no
accumulation of duplicate "Personal Hybrid Cloud" projects. Activating it
requires `SPRING_PROFILES_ACTIVE=dev` (or `--spring.profiles.active=dev`),
which is not part of the default Compose stack or any documented
production configuration.

## Revisit criteria

Revisit if a future milestone wants seed data for automated end-to-end
tests (Milestone 7) — the current seed targets manual development
exploration, and a test-oriented seed might need smaller, more
deterministic fixtures instead of the full representative dataset.
