# HomeCloud Planner Build Plan

## Product goal

Create a locally hosted application that tracks the planning, purchasing, installation, configuration, validation, operation, and future upgrades of a personal hybrid cloud.

## Selected stack

### Backend

- Java 21
- Spring Boot 3.x
- Gradle
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring Boot Actuator
- PostgreSQL
- Flyway
- springdoc-openapi
- Testcontainers

### Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form
- Zod
- Material UI
- Recharts

### Runtime

- Docker Compose for household production services
- Kubernetes only for selected lab workloads
- PostgreSQL as the system of record

## MVP capabilities

- Dashboard
- Roadmap and tasks
- Shopping list and budget
- Hardware inventory
- Service catalog
- Backup matrix
- Architecture decision records
- Markdown documentation
- JSON and Markdown export

## Milestones

1. Repository and development environment
2. Projects, phases, and tasks
3. Shopping and budget
4. Hardware inventory and service catalog
5. Backup matrix and architecture decisions
6. Dashboard and export
7. Authentication, testing, and deployment hardening

## Definition of done

A feature is complete when it has a database migration, backend tests, frontend support, loading/error/empty states, updated documentation, and a passing CI build.
