# Application Architecture

## Decision summary

HomeCloud Planner is a modular monolith with a separately built React frontend and Spring Boot backend.

## Boundaries

- The backend owns business rules and persistence.
- The frontend owns interaction and presentation.
- PostgreSQL is the system of record.
- Uploaded documents are stored outside PostgreSQL; the database stores metadata and references.
- External integrations are isolated behind interfaces.
- Docker Compose is the primary deployment mechanism.

## Future integrations

Potential adapters include UniFi, Synology, Home Assistant, Prometheus, Docker, Tailscale, and cloud backup providers. These are not part of the MVP.

## Domain modules

Backend packages follow package-by-feature (`com.homecloud.planner.<feature>`). As of Milestone 6:

- `project` — projects (status, budget, start/target dates).
- `phase` — ordered phases within a project (contiguous sequence, reorder, restrictive delete).
- `task` — tasks within a phase and their dependencies.
- `roadmap` — read-only aggregation across the three above: computed blocked state, computed progress summaries, and the combined `GET /projects/{id}/roadmap` payload the frontend uses to render the Roadmap page in one request. See [ADR-0002](decisions/ADR-0002-roadmap-domain-behavior.md) for the deletion, dependency, blocked-state, and progress rules.
- `shopping` — purchase items (the shopping list) belonging to a project and optionally tagged to a phase.
- `budget` — read-only aggregation over `shopping`: computed estimated/actual/committed totals, remaining budget, and category summaries via `GET /projects/{id}/budget`. See [ADR-0003](decisions/ADR-0003-budget-calculation-behavior.md).
- `device` — hardware inventory belonging to a project.
- `servicecatalog` — `ManagedService` (the domain name for what the catalog calls "services"), its dependency graph, and dependency-cycle/deletion rules. See [ADR-0004](decisions/ADR-0004-service-dependency-behavior.md).
- `backup` — `BackupPolicy` records (the backup matrix) and computed coverage/warning state. See [ADR-0005](decisions/ADR-0005-backup-coverage-rules.md).
- `decision` — `ArchitectureDecision` records (ADR-style, with many-to-many links to `device` and `servicecatalog`).
- `dashboard` — read-only aggregation across every other domain package into one `GET /projects/{id}/dashboard` payload, plus `RecommendedActionEngine` (deterministic, rule-based "next actions" — never AI-generated).
- `export` — reuses the existing response DTOs from every other package to assemble `GET /export/json` and render `GET /export/markdown`. See [ADR-0007](decisions/ADR-0007-export-format.md).
- `seed` — `@Profile("dev")`-gated, development-only data population (`POST /dev/seed`); the beans do not exist outside that profile. See [ADR-0006](decisions/ADR-0006-seed-data-strategy.md).
- `common` — cross-feature building blocks with no domain of their own: `NotFoundException` / `ConflictException` / `InvalidRequestException` (mapped to RFC 9457 responses by `ApiExceptionHandler`), and `DependencyGraphs` (the shared cycle-detection algorithm used by both `task` and `servicecatalog`).

Feature packages depend directly on each other's repositories and services where needed (e.g. `project` checks `phase`, `shopping`, `device`, `servicecatalog`, `backup`, and `decision` repositories before allowing a delete; `decision` reads `device` and `servicecatalog` entities to populate its many-to-many links; `dashboard`, `export`, and `seed` each read across nearly every domain package) — this is a modular monolith, not a set of isolated services, so that coupling is intentional rather than a boundary violation.

The frontend mirrors this with `pages/roadmap/`, `pages/shopping/`, `pages/hardware/`, `pages/services/`, `pages/backup/`, `pages/decisions/`, `pages/dashboard/`, and `pages/settings/` modules, each composing a project selector, summary cards, list/table/card views, filters, and create/edit dialogs backed by TanStack Query hooks (`api/projects.ts`, `api/phases.ts`, `api/tasks.ts`, `api/roadmap.ts`, `api/purchaseItems.ts`, `api/budget.ts`, `api/devices.ts`, `api/services.ts`, `api/backupPolicies.ts`, `api/decisions.ts`, `api/dashboard.ts`, `api/export.ts`). Which project is "current" is shared across pages via `pages/shared/SelectedProjectContext.tsx` (a small React Context) rather than each page defaulting independently — local per-page state was tried first and produced a real bug where navigating between pages silently switched the active project back to the first one in the list. Service and decision detail views are routed pages (`/services/:serviceId`, `/decisions/:decisionId`) rather than dialogs, since both need room for a dependency graph or long-form content that a modal doesn't comfortably give. The Settings page (`/settings`) hosts backend health status and the export download controls; `api/export.ts` triggers a browser download via `fetch` + blob rather than a plain `<a href>`, since the export endpoints require no query parameters but still benefit from consistent error handling on failure.
