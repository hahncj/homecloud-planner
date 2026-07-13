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

Backend packages follow package-by-feature (`com.homecloud.planner.<feature>`). As of Milestone 2:

- `project` — projects (status, budget, start/target dates).
- `phase` — ordered phases within a project (contiguous sequence, reorder, restrictive delete).
- `task` — tasks within a phase and their dependencies.
- `roadmap` — read-only aggregation across the three above: computed blocked state, computed progress summaries, and the combined `GET /projects/{id}/roadmap` payload the frontend uses to render the Roadmap page in one request. See [ADR-0002](decisions/ADR-0002-roadmap-domain-behavior.md) for the deletion, dependency, blocked-state, and progress rules.

The frontend mirrors this with a `pages/roadmap/` module: `RoadmapPage` composes a project selector, summary cards, a phase board, a task table, filters, and create/edit dialogs backed by `api/projects.ts`, `api/phases.ts`, `api/tasks.ts`, and `api/roadmap.ts` (TanStack Query hooks over the REST API).
