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

## Milestone 5 — Backup matrix and architecture decisions

- [x] BackupPolicy entity and API (name, data category, primary/local/off-site locations, encryption, sensitivity flag, frequency, retention, RPO/RTO, last verified date, verification notes; CRUD + coverage/verification filtering)
- [x] Backup matrix UI (coverage indicators, missing-local/missing-offsite/missing-encryption/verification-overdue warnings, filters, explicit RAID/snapshot disclaimer — see [ADR-0005](docs/decisions/ADR-0005-backup-coverage-rules.md))
- [x] ArchitectureDecision entity and API (title, status, context, decision, alternatives considered, consequences, decision date, revisit criteria, related devices/services; CRUD + status filtering)
- [x] Architecture decisions UI (decision list, routed decision detail page, create/edit forms, markdown-friendly plain-text content fields)

## Milestone 6 — Dashboard, seed data, and export

- [x] Dashboard API (progress, current phase, budget figures, blocked/upcoming/recent tasks, purchase/device/service status counts, backup coverage warnings, upcoming warranty expirations, deterministic rule-based recommended actions — see `RecommendedActionEngine`)
- [x] Dashboard UI (project selector, summary cards, per-section cards for every computed field above)
- [x] Seed data (repeatable `POST /dev/seed`, content drawn from the sibling personal-cloud-docs repo, only registered under the `dev` Spring profile — see [ADR-0006](docs/decisions/ADR-0006-seed-data-strategy.md))
- [x] JSON and Markdown export (`GET /export/json`, `GET /export/markdown`, reusing existing response DTOs so credentials can never leak through — see [ADR-0007](docs/decisions/ADR-0007-export-format.md))
- [x] Settings page with export download controls

## Milestone 7 — Authentication, testing, and deployment hardening

- [x] Single local admin account, BCrypt-hashed, created only from `ADMIN_USERNAME`/`ADMIN_PASSWORD` at first startup, no default credentials anywhere — see [ADR-0008](docs/decisions/ADR-0008-authentication-approach.md)
- [x] Session-cookie login/logout/session endpoints (`POST /auth/login`, `POST /auth/logout`, `GET /auth/session`), RFC 9457 problem details for 401/403
- [x] Double-submit cookie CSRF protection (`XSRF-TOKEN` cookie, `X-XSRF-TOKEN` header), `CorsConfigurationSource` shared between Spring MVC and Spring Security with `allowCredentials(true)`
- [x] Security hardening: `X-Frame-Options: DENY`, request body size limits, actuator restricted to `health` with no detail, Postgres port not published in the base Compose file, Docker container non-root review — see `docs/security.md`
- [x] Backend test suite retrofitted for authenticated/CSRF-protected requests; dedicated `AuthenticationIT` covering login success/failure, logout, session, 401s, and CSRF rejection
- [x] Frontend `AuthContext`, login page, protected routes, logout, `credentials: 'include'` + CSRF header on every mutating request, global 401 handling
- [x] Docker/Compose hardening: `compose.override.yaml` for local-only Postgres port publishing, `restart: unless-stopped`, admin credentials passed through Compose
- [x] CI: Docker image build validation for both backend and frontend images
- [x] Playwright e2e suite covering login, create project, add phase, add task, add dependency, confirm blocked behavior, add purchase, add device, add service, add backup policy, view dashboard, export project — see `frontend/e2e/README.md`
- [x] Documentation: `docs/security.md` (deployment model, cookies/CSRF, CORS, actuator, container users, Postgres exposure), `docs/backup-restore.md` (Planner's own database), future Authentik/OIDC path documented in ADR-0008
