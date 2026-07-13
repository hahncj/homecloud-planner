# API Reference

All endpoints are served under `/api/v1`. Errors follow RFC 9457
(`application/problem+json`) with `status`, `title`, and `detail`.

## Health

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Liveness check; returns `{ status, timestamp }`. |

## Projects

| Method | Path | Description |
|---|---|---|
| GET | `/projects` | List all projects, each with a computed `progress` summary. |
| POST | `/projects` | Create a project. |
| GET | `/projects/{projectId}` | Fetch one project. |
| PUT | `/projects/{projectId}` | Replace a project's editable fields. |
| DELETE | `/projects/{projectId}` | Delete a project. `409` if it still has phases. |
| GET | `/projects/{projectId}/roadmap` | Combined payload: the project, its ordered phases (with progress), and its full task list (with computed `blocked` flags). Powers the Roadmap page in a single request. |

Request body (`ProjectRequest`): `name` (required, ≤200 chars), `description`,
`status` (`PLANNING` \| `IN_PROGRESS` \| `ON_HOLD` \| `COMPLETED` \| `CANCELLED`),
`budget` (≥0 or null), `startDate`, `targetDate` (ISO dates or null).

## Phases

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/phases` | List phases in sequence order, each with a computed `progress` summary. |
| POST | `/projects/{projectId}/phases` | Create a phase; it is appended at the next sequence number. |
| PUT | `/projects/{projectId}/phases/{phaseId}` | Rename / edit description. Sequence is not editable here. |
| DELETE | `/projects/{projectId}/phases/{phaseId}` | Delete a phase. `409` if it still has tasks. Remaining phases are re-sequenced to stay contiguous. |
| PUT | `/projects/{projectId}/phases/reorder` | Body: `{ "orderedPhaseIds": [uuid, ...] }` — must contain every phase of the project exactly once. Reassigns sequence 1..N in the given order. |

Request body (`PhaseRequest`): `name` (required, ≤200 chars), `description`.

## Tasks

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/tasks` | List tasks in the project. Query params: `phaseId`, `status`, `priority`, `blocked` (all optional filters). |
| POST | `/projects/{projectId}/phases/{phaseId}/tasks` | Create a task in the given phase. |
| GET | `/tasks/{taskId}` | Fetch one task. |
| PUT | `/tasks/{taskId}` | Replace a task's editable fields. A task's phase cannot be changed via this endpoint. |
| DELETE | `/tasks/{taskId}` | Delete a task (and its dependency edges). |

Request body (`TaskRequest`): `title` (required, ≤200 chars), `description`,
`status` (`NOT_STARTED` \| `IN_PROGRESS` \| `COMPLETED` \| `CANCELLED`),
`priority` (`LOW` \| `MEDIUM` \| `HIGH` \| `CRITICAL`), `estimatedCost`,
`actualCost` (≥0 or null), `targetDate`, `completedDate`,
`acceptanceCriteria`, `notes`.

Response (`TaskResponse`) additionally includes `blocked` (computed) and
`dependsOnTaskIds` (computed from the dependency graph).

## Task dependencies

| Method | Path | Description |
|---|---|---|
| GET | `/tasks/{taskId}/dependencies` | List a task's direct dependencies. |
| POST | `/tasks/{taskId}/dependencies` | Body: `{ "dependsOnTaskId": uuid }`. Rejects self-reference, duplicates, cross-project references, and cycles — see [ADR-0002](decisions/ADR-0002-roadmap-domain-behavior.md). |
| DELETE | `/tasks/{taskId}/dependencies/{dependsOnTaskId}` | Remove a dependency edge. |

## Progress summary shape

Projects and phases both return a `progress` object:

```json
{ "taskCount": 4, "completedCount": 2, "blockedCount": 1, "progressPercentage": 50 }
```

Cancelled tasks are excluded from `taskCount` and `completedCount`.
`progressPercentage` is `round(completedCount / taskCount * 100)`, or `0`
when `taskCount` is `0`.
