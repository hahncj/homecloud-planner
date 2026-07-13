# HomeCloud Planner

The project-management and operations application for the personal hybrid cloud.

## First implementation prompt

Use this with ChatGPT Codex, GitHub Copilot Agent, or Claude Code:

> Read `AI_CONTEXT.md`, `BUILD_PLAN.md`, `TASKS.md`, and all ADRs. Implement only Milestone 1. Start with a working vertical slice: PostgreSQL, Spring Boot, React, Flyway, Docker Compose, CI, and `GET /api/v1/health`. Add tests and update documentation. Stop after all Milestone 1 validation steps pass.

## Prerequisites

Local (non-Docker) development requires:

- **Java 21** — the backend's Gradle toolchain builds and runs on Java 21.
- **Node 22 (LTS)** — the version the frontend's tooling (Vite, Vitest) is
  built and tested against; see `frontend/.nvmrc` and `frontend/package.json`
  `engines.node`.

Docker Compose is unaffected by your local Java/Node versions since it builds
inside pinned container images.

## Planned local ports

| Component | Port |
|---|---:|
| Frontend | 3000 |
| Backend | 8080 |
| PostgreSQL | 5432 |

## Environment configuration

All runtime configuration is read from a root-level `.env` file, which is
gitignored so credentials are never committed. Copy the template and adjust
values as needed:

```bash
cp .env.example .env
```

`.env.example` documents every variable used by Docker Compose, the backend,
and the frontend. `docker compose` and Vite (`envDir: '..'` in
`frontend/vite.config.ts`) both load this same file, so one `.env` covers the
full stack and local development.

## Running the complete stack with Docker Compose

Builds and runs PostgreSQL, the backend, and the frontend as containers.
`compose.yaml` gives each service a health check, and `depends_on: condition:
service_healthy` so containers start in dependency order: PostgreSQL becomes
healthy before the backend starts, and the backend becomes healthy before the
frontend starts.

```bash
cp .env.example .env        # first time only
docker compose build        # build backend and frontend images
docker compose up -d        # start postgres, backend, frontend
docker compose ps           # confirm all three services report "healthy"
```

Verify the stack:

```bash
curl http://localhost:8080/api/v1/health
open http://localhost:3000
```

Stop the stack (the named `homecloud-postgres` volume persists data across
restarts):

```bash
docker compose down
```

Equivalent `make` targets: `make build`, `make up`, `make ps`, `make logs`,
`make down`.

## Local development (PostgreSQL in Docker, backend/frontend in VS Code)

Run only PostgreSQL in Docker and run the backend and frontend directly from
your machine or VS Code for the fastest edit-reload loop:

```bash
docker compose up -d postgres     # or: make db-up
```

Backend (Spring Boot, defaults to `localhost:5432` when no `SPRING_DATASOURCE_*`
environment variables are set). Unlike Docker Compose and Vite, `./gradlew
bootRun` does **not** read the root `.env` file automatically, so any
customized values in `.env` (e.g. `CORS_ALLOWED_ORIGINS`) must be exported
into the shell first:

```bash
cd backend
set -a && source ../.env && set +a
./gradlew bootRun
```

Or, from the repository root, use the Make target that does this for you:

```bash
make backend-run
```

Frontend (Vite dev server on port 3000, reads `VITE_API_BASE_URL` from the
root `.env`):

```bash
cd frontend
npm install
npm run dev
```

Stop PostgreSQL when finished:

```bash
docker compose down               # or: make db-down
```

## Recommended package structure

```text
com.homecloud.planner
├── common
├── dashboard
├── project
├── phase
├── task
├── shopping
├── budget
├── device
├── servicecatalog
├── backup
├── decision
├── document
└── health
```
