# Tasks

## Milestone 1 — Foundation

- [x] Create Spring Boot 3.x Gradle project using Java 21.
- [x] Create Vite React TypeScript project.
- [x] Add PostgreSQL to `compose.yaml`.
- [x] Add Flyway baseline migration.
- [x] Add `GET /api/v1/health`.
- [x] Display backend health in the frontend.
- [x] Add backend and frontend tests.
- [x] Add Dockerfiles and complete Compose stack.
- [x] Add GitHub Actions workflows.
- [x] Verify clean startup from documented commands.

## Milestone 2 — Roadmap

- [x] Project entity and API (CRUD, validation, status, budget, dates; restrictive delete while phases exist)
- [x] Phase entity and API (ordered, contiguous sequence, create/edit/delete/reorder, restrictive delete while tasks exist)
- [x] Task entity and API (CRUD, status, priority, costs, dates, acceptance criteria, notes, filtering)
- [x] Task dependencies (self/duplicate/cross-project/cycle rejection, cross-phase allowed)
- [x] Roadmap UI (project selector, summary cards, phase progress, board view, table view, filters, blocked indicators)
- [x] Progress calculations (phase and project, computed not persisted, cancelled tasks excluded)

## Milestone 3 — Shopping and budget

- [ ] Purchase items
- [ ] Estimated and actual costs
- [ ] Budget summary
- [ ] Category totals
- [ ] Warranty fields
