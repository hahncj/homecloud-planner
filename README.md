# HomeCloud Planner

The project-management and operations application for the personal hybrid cloud.

## First implementation prompt

Use this with ChatGPT Codex, GitHub Copilot Agent, or Claude Code:

> Read `AI_CONTEXT.md`, `BUILD_PLAN.md`, `TASKS.md`, and all ADRs. Implement only Milestone 1. Start with a working vertical slice: PostgreSQL, Spring Boot, React, Flyway, Docker Compose, CI, and `GET /api/v1/health`. Add tests and update documentation. Stop after all Milestone 1 validation steps pass.

## Planned local ports

| Component | Port |
|---|---:|
| Frontend | 3000 |
| Backend | 8080 |
| PostgreSQL | 5432 |

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
