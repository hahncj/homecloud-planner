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
| DELETE | `/projects/{projectId}` | Delete a project. `409` if it still has phases or purchase items. |
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

## Purchase items

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/purchase-items` | List purchase items in the project. Query params: `status`, `category`, `phaseId` (all optional filters). |
| POST | `/projects/{projectId}/purchase-items` | Create a purchase item. |
| GET | `/purchase-items/{purchaseItemId}` | Fetch one purchase item. |
| PUT | `/purchase-items/{purchaseItemId}` | Replace a purchase item's editable fields. |
| DELETE | `/purchase-items/{purchaseItemId}` | Delete a purchase item. |

Request body (`PurchaseItemRequest`): `phaseId` (optional, must belong to
the same project), `category` (required, ≤100 chars, free text),
`productName` (required, ≤200 chars), `manufacturer`, `model`,
`description`, `quantity` (required, integer ≥1), `estimatedUnitPrice`,
`actualUnitPrice` (≥0 or null), `vendor`, `purchaseUrl` (valid URL or
null), `status` (`IDEA` \| `RESEARCHING` \| `PLANNED` \| `ORDERED` \|
`RECEIVED` \| `INSTALLED` \| `CANCELLED`), `purchaseDate`, `deliveryDate`,
`warrantyExpiration` (ISO dates or null), `receiptReference`, `notes`.

Response (`PurchaseItemResponse`) additionally includes `estimatedTotal`
and `actualTotal` (both computed as `unitPrice × quantity`, `null` when the
corresponding unit price is `null`).

## Budget

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/budget` | Computed budget summary for the project. |

Response (`BudgetSummary`):

```json
{
  "budget": "1000.00",
  "estimatedTotal": "400.00",
  "actualTotal": "90.00",
  "committedSpending": "390.00",
  "remainingBudget": "610.00",
  "categories": [
    { "category": "Networking", "itemCount": 2, "estimatedTotal": "100.00", "actualTotal": "90.00", "committedSpending": "90.00" }
  ]
}
```

Cancelled purchase items are excluded from every figure. `committedSpending`
uses each item's actual cost when known, falling back to its estimate.
`remainingBudget` is `null` when the project has no budget set. See
[ADR-0003](decisions/ADR-0003-budget-calculation-behavior.md).

## Devices

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/devices` | List devices in the project. Query params: `lifecycleStatus`, `role`, `location` (all optional filters). |
| POST | `/projects/{projectId}/devices` | Create a device. |
| GET | `/devices/{deviceId}` | Fetch one device. |
| PUT | `/devices/{deviceId}` | Replace a device's editable fields. |
| DELETE | `/devices/{deviceId}` | Delete a device. Any service that lists it as a host device has that reference cleared, not blocked. |

Request body (`DeviceRequest`): `name` (required, ≤200 chars),
`manufacturer`, `model`, `serialNumber`, `role`, `location`, `hostname`,
`ipAddress` (valid IPv4/IPv6 or null), `macAddress` (valid MAC or null),
`vlan` (integer 1–4094 or null), `operatingSystem`, `firmwareVersion`,
`purchaseDate`, `warrantyExpiration`, `lifecycleStatus` (required —
`PLANNED` \| `ACTIVE` \| `SPARE` \| `MAINTENANCE` \| `RETIRED` \|
`DISPOSED`), `replacementTarget` (ISO date or null), `notes`. All network
fields are optional; when present, they're validated.

## Services

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/services` | List services in the project. Query params: `status`, `runtimeType`, `sensitivity`, `externallyExposed` (all optional filters). |
| POST | `/projects/{projectId}/services` | Create a service. |
| GET | `/services/{serviceId}` | Fetch one service, including its direct `dependsOnServiceIds`. |
| PUT | `/services/{serviceId}` | Replace a service's editable fields. |
| DELETE | `/services/{serviceId}` | Delete a service. `409` if another service still depends on it — see [ADR-0004](decisions/ADR-0004-service-dependency-behavior.md). |

Request body (`ManagedServiceRequest`): `hostDeviceId` (optional, must
belong to the same project), `name` (required, ≤200 chars), `purpose`,
`description`, `status` (required — `PLANNED` \| `INSTALLING` \|
`CONFIGURING` \| `VALIDATING` \| `OPERATIONAL` \| `DEGRADED` \| `DISABLED`
\| `RETIRED`), `runtimeType` (required — `DOCKER` \| `KUBERNETES` \|
`VIRTUAL_MACHINE` \| `BARE_METAL` \| `MANAGED_CLOUD` \| `NAS_NATIVE` \|
`OTHER`), `storageLocation`, `sensitivity` (required — `PUBLIC` \|
`INTERNAL` \| `CONFIDENTIAL` \| `HIGHLY_SENSITIVE`), `externallyExposed`
(boolean), `authenticationMethod`, `backupPolicy` (free text describing
how the service is backed up — intentionally not an FK to `BackupPolicy`
below; see [ADR-0005](decisions/ADR-0005-backup-coverage-rules.md)),
`documentationUrl`, `repositoryUrl` (valid URLs or null), `notes`.

## Service dependencies

| Method | Path | Description |
|---|---|---|
| GET | `/services/{serviceId}/dependencies` | List a service's direct dependencies. |
| POST | `/services/{serviceId}/dependencies` | Body: `{ "dependsOnServiceId": uuid }`. Rejects self-reference, duplicates, and cycles. |
| DELETE | `/services/{serviceId}/dependencies/{dependsOnServiceId}` | Remove a dependency edge. |

## Backup policies

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/backup-policies` | List backup policies (the backup matrix) for the project. Query params: `coverageState`, `verificationOverdue` (both optional filters). |
| POST | `/projects/{projectId}/backup-policies` | Create a backup policy. |
| GET | `/backup-policies/{backupPolicyId}` | Fetch one backup policy. |
| PUT | `/backup-policies/{backupPolicyId}` | Replace a backup policy's editable fields. |
| DELETE | `/backup-policies/{backupPolicyId}` | Delete a backup policy. |

Request body (`BackupPolicyRequest`): `name` (required, ≤200 chars),
`dataCategory`, `primaryLocation` (required), `localBackupLocation`,
`offsiteBackupLocation` (optional), `encrypted`, `containsSensitiveData`
(booleans), `frequency` (required — `CONTINUOUS` \| `HOURLY` \| `DAILY` \|
`WEEKLY` \| `MONTHLY` \| `MANUAL`), `retention`, `recoveryPointObjective`,
`recoveryTimeObjective` (free text), `lastVerifiedDate` (ISO date or
null), `verificationNotes`.

Response (`BackupPolicyResponse`) additionally includes computed
`coverageState` (`NONE` \| `PARTIAL` \| `FULL`), `missingLocalBackup`,
`missingOffsiteBackup`, `missingEncryptionForSensitiveOffsite`, and
`verificationOverdue` — see
[ADR-0005](decisions/ADR-0005-backup-coverage-rules.md) for the rules.

## Architecture decisions

| Method | Path | Description |
|---|---|---|
| GET | `/projects/{projectId}/decisions` | List decisions for the project. Query param: `status` (optional filter). |
| POST | `/projects/{projectId}/decisions` | Create a decision. |
| GET | `/decisions/{decisionId}` | Fetch one decision, including its related devices and services. |
| PUT | `/decisions/{decisionId}` | Replace a decision's editable fields and related entities. |
| DELETE | `/decisions/{decisionId}` | Delete a decision. |

Request body (`ArchitectureDecisionRequest`): `title` (required, ≤200
chars), `status` (required — `PROPOSED` \| `ACCEPTED` \| `DEPRECATED` \|
`SUPERSEDED` \| `REJECTED`), `context`, `decision` (required — the
content fields are plain multiline text, kept markdown-friendly by never
being fed through a rich-text editor), `alternativesConsidered`,
`consequences`, `decisionDate` (ISO date or null), `revisitCriteria`,
`relatedDeviceIds`, `relatedServiceIds` (lists of UUIDs, must belong to
the same project).

Response (`ArchitectureDecisionResponse`) includes `relatedDevices` and
`relatedServices` as `{ id, name }` summaries rather than full device/
service payloads.
