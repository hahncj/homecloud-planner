# AI Context

## Mission

Build HomeCloud Planner as a maintainable modular monolith. Prefer simple, reviewable changes over broad autonomous rewrites.

## Rules

- Read `BUILD_PLAN.md`, `TASKS.md`, and relevant ADRs before coding.
- Work on one milestone or narrowly scoped task at a time.
- Do not expose JPA entities through REST APIs.
- Use Java records for API DTOs where appropriate.
- Use constructor injection.
- Use `BigDecimal` for money, `Instant` for timestamps, and `LocalDate` for date-only fields.
- Use Flyway for every schema change.
- Return RFC 9457 problem details for API errors.
- Enable TypeScript strict mode and avoid `any`.
- Use TanStack Query for server state and React Hook Form plus Zod for forms.
- Add tests with each change.
- Update documentation and ADRs when architecture changes.
- Never commit secrets.

## Current priority

Milestones 1–3 are complete (foundation; projects/phases/tasks/dependencies/progress with the Roadmap UI; shopping list and budget tracking). Next up is Milestone 4: hardware inventory and service catalog.
