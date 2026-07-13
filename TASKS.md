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

- [x] Purchase items (category, product/manufacturer/model, description, quantity, vendor, purchase URL, status, dates, receipt reference, related phase, notes; CRUD + status/category/phase filtering)
- [x] Estimated and actual costs (positive quantity, non-negative prices; totals computed as unit price × quantity, never persisted)
- [x] Budget summary (estimated vs. actual totals, committed spending preferring actual over estimate, remaining budget; cancelled items excluded — see [ADR-0003](docs/decisions/ADR-0003-budget-calculation-behavior.md))
- [x] Category totals (grouped estimated/actual/committed per free-text category)
- [x] Warranty fields (warranty expiration captured; expired/expiring-soon/active indicator computed client-side)

## Milestone 4 — Hardware inventory and service catalog

- [x] Device entity and API (name, manufacturer/model/serial, role, location, hostname, validated optional IP/MAC, VLAN, OS, firmware, purchase/warranty/replacement dates, lifecycle status, notes; CRUD + lifecycle/role/location filtering)
- [x] Hardware inventory UI (device cards, table view, device detail view, filters, warranty and lifecycle indicators)
- [x] ManagedService entity and API (purpose, description, status, runtime type, host device, storage location, sensitivity, external exposure, authentication method, backup policy text, documentation/repository URLs, notes; CRUD + status/runtime/sensitivity/exposure filtering)
- [x] Service dependencies (self/duplicate/cycle rejection; deletion blocked while other services depend on it — see [ADR-0004](docs/decisions/ADR-0004-service-dependency-behavior.md))
- [x] Service catalog UI (catalog grid, routed service detail page, dependency list, runtime/host info, sensitivity and exposure indicators)
