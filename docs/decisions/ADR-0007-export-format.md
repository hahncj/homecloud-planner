# ADR-0007: Export format

## Status

Accepted

## Context

Milestone 6 requires `GET /api/v1/export/json` and
`GET /api/v1/export/markdown`, covering every project's phases, tasks,
dependencies, purchases, devices, services, backup policies, and
decisions, in a consistent structure, without ever including credentials
or authentication data.

## Decision

- **The export reuses the existing REST response DTOs** (`ProjectResponse`,
  `PhaseResponse`, `TaskResponse`, `PurchaseItemResponse`,
  `DeviceResponse`, `ManagedServiceResponse`, `BackupPolicyResponse`,
  `ArchitectureDecisionResponse`) rather than serializing JPA entities or
  defining a parallel set of export-only DTOs. This guarantees the export
  can never contain a field that hasn't already been reviewed for the
  public API contract — there is exactly one place (`ApiExceptionHandler`
  aside) where "what does the outside world see" is decided, so the "no
  credentials" requirement is enforced by construction rather than by a
  second manually-maintained allowlist that could drift from the API.
  Today there are no credential fields anywhere in the domain (Milestone
  7 adds the first one, a password hash on the future admin-account
  entity); whoever builds that field must remember not to expose it
  through a `*Response` record, and by extension it will automatically
  stay out of the export too.

- **Dependency edges are derived, not separately queried.** Both
  `TaskResponse` and `ManagedServiceResponse` already carry their direct
  dependency IDs (`dependsOnTaskIds` / `dependsOnServiceIds`). The export
  flattens those into a generic `DependencyEdge(from, to)` list per
  project instead of adding new repository methods — the data was already
  being fetched.

- **One JSON structure, one project list.** `ExportBundle` is
  `{ exportedAt, projects: [ProjectExport...] }`, and each `ProjectExport`
  is fully self-contained (its own phases, tasks, purchases, etc.) so a
  consumer can process one project's export without cross-referencing
  another. There is no `projectId` query parameter to scope the export to
  a single project — the endpoint always exports everything, which is
  what "export all project-related data" asks for and is simple to reason
  about; scoping can be added later as a query parameter without a
  breaking change if a real need for it shows up.

- **Markdown is a rendered view of the same `ExportBundle`, not a
  second data-gathering path.** `MarkdownExportRenderer` takes the exact
  object `ExportController` also serializes to JSON, so the two export
  formats can never disagree about what data was included — only about
  how it's presented (tables for structured lists, a checklist for tasks,
  prose sections for decisions).

- **Both endpoints set `Content-Disposition: attachment`** with a fixed
  filename, so a direct browser navigation to either URL downloads a file
  instead of rendering inline, and the Settings page's download buttons
  work by triggering a `fetch` + blob download using that same filename
  (falling back to a default name if the header is ever missing).

## Alternatives considered

- Serializing JPA entities directly (with `@JsonIgnore` on sensitive
  fields). Rejected — this is the same reasoning as "never expose JPA
  entities through REST APIs" applied to exports; a second suppression
  mechanism to keep in sync is more failure-prone than reusing the
  already-reviewed response DTOs.
- A `projectId` path/query parameter required on every export call.
  Rejected for now per the "one project list" reasoning above; the
  export always covers everything, matching the literal requirement.

## Consequences

Adding a new field to any response DTO automatically appears in the
export with no additional code; removing or renaming a field is a single
change site. The export endpoints do O(projects × entities) work per
request with no caching, which is appropriate at this application's
scale and avoids a second stale-cache problem to manage.

## Revisit criteria

Revisit if the export needs to scale beyond a single household's data (at
which point caching or pagination might matter), or if a future
authentication milestone introduces a field that must be actively
stripped rather than simply never added to a response DTO in the first
place.
