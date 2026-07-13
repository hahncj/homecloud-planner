# Data Model

PostgreSQL is the system of record. Every schema change is a Flyway
migration under `backend/src/main/resources/db/migration/`.

## V1 — baseline

`schema_baseline` — a single-row table that proves Flyway is wired up.
Carries no domain data.

## V2 — roadmap (Milestone 2)

```
project 1───* phase 1───* task *───* task   (via task_dependency)
```

### `project`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `name` | `varchar(200)` | Required. |
| `description` | `text` | Nullable. |
| `status` | `varchar(20)` | `PLANNING` \| `IN_PROGRESS` \| `ON_HOLD` \| `COMPLETED` \| `CANCELLED`. |
| `budget` | `numeric(12,2)` | Nullable; `>= 0` when present. |
| `start_date`, `target_date` | `date` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | Managed by Hibernate. |

### `phase`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `project_id` | `uuid` | FK → `project`, `ON DELETE RESTRICT`. |
| `name` | `varchar(200)` | Required. |
| `description` | `text` | Nullable. |
| `sequence` | `integer` | `>= 1`; unique per project; kept contiguous by the application (see [ADR-0002](decisions/ADR-0002-roadmap-domain-behavior.md)). |
| `created_at`, `updated_at` | `timestamptz` | |

### `task`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `phase_id` | `uuid` | FK → `phase`, `ON DELETE RESTRICT`. |
| `title` | `varchar(200)` | Required. |
| `description` | `text` | Nullable. |
| `status` | `varchar(20)` | `NOT_STARTED` \| `IN_PROGRESS` \| `COMPLETED` \| `CANCELLED`. |
| `priority` | `varchar(20)` | `LOW` \| `MEDIUM` \| `HIGH` \| `CRITICAL`. |
| `estimated_cost`, `actual_cost` | `numeric(12,2)` | Nullable; `>= 0` when present. |
| `target_date`, `completed_date` | `date` | Nullable. |
| `acceptance_criteria`, `notes` | `text` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | |

Note: there is no `blocked` column. Blocked state is always computed from
`task_dependency` at read time.

### `task_dependency`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `task_id` | `uuid` | FK → `task`, `ON DELETE CASCADE`. The dependent task. |
| `depends_on_task_id` | `uuid` | FK → `task`, `ON DELETE CASCADE`. The prerequisite task. |
| `created_at` | `timestamptz` | |

Constraints: `task_id <> depends_on_task_id` (no self-dependency);
`UNIQUE (task_id, depends_on_task_id)` (no duplicate edges). Cross-project
references and cycles are rejected at the application layer, since
expressing a project-scoped, acyclic-graph constraint in SQL is impractical.

Indexes: `phase(project_id)`, `task(phase_id)`, `task(status)`,
`task_dependency(task_id)`, `task_dependency(depends_on_task_id)`.

## V3 — shopping (Milestone 3)

### `purchase_item`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `project_id` | `uuid` | FK → `project`, `ON DELETE RESTRICT`. |
| `phase_id` | `uuid` | FK → `phase`, `ON DELETE SET NULL`. Optional "related phase" tag; deleting the phase just clears the tag rather than blocking the delete or destroying the purchase item. |
| `category` | `varchar(100)` | Required; free text (no fixed enum — categories aren't known ahead of time). |
| `product_name` | `varchar(200)` | Required. |
| `manufacturer`, `model` | `varchar(200)` | Nullable. |
| `description` | `text` | Nullable. |
| `quantity` | `integer` | `>= 1`. |
| `estimated_unit_price`, `actual_unit_price` | `numeric(12,2)` | Nullable; `>= 0` when present. |
| `vendor` | `varchar(200)` | Nullable. |
| `purchase_url` | `varchar(2048)` | Nullable; validated as a URL when present. |
| `status` | `varchar(20)` | `IDEA` \| `RESEARCHING` \| `PLANNED` \| `ORDERED` \| `RECEIVED` \| `INSTALLED` \| `CANCELLED`. |
| `purchase_date`, `delivery_date`, `warranty_expiration` | `date` | Nullable. |
| `receipt_reference` | `varchar(500)` | Nullable; a free-text reference (no file upload — deferred). |
| `notes` | `text` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | |

Note: there are no `estimated_total` / `actual_total` columns. Both are
always computed as `unit_price × quantity` at read time — see
[ADR-0003](decisions/ADR-0003-budget-calculation-behavior.md).

Indexes: `purchase_item(project_id)`, `purchase_item(phase_id)`,
`purchase_item(status)`, `purchase_item(category)`.

A project cannot be deleted while it has purchase items (same restrictive,
non-cascading pattern as phases — see ADR-0002).

## V4 — hardware and services (Milestone 4)

### `device`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `project_id` | `uuid` | FK → `project`, `ON DELETE RESTRICT`. |
| `name` | `varchar(200)` | Required. |
| `manufacturer`, `model`, `serial_number` | `varchar(200)` | Nullable. |
| `role` | `varchar(100)` | Nullable; free text (e.g. "Router", "NAS"). |
| `location` | `varchar(200)` | Nullable; free text. |
| `hostname` | `varchar(255)` | Nullable. |
| `ip_address` | `varchar(45)` | Nullable; validated as IPv4/IPv6 when present. |
| `mac_address` | `varchar(17)` | Nullable; validated as a MAC address when present. |
| `vlan` | `integer` | Nullable; `1`–`4094` when present. |
| `operating_system`, `firmware_version` | `varchar` | Nullable. |
| `purchase_date`, `warranty_expiration`, `replacement_target` | `date` | Nullable. |
| `lifecycle_status` | `varchar(20)` | `PLANNED` \| `ACTIVE` \| `SPARE` \| `MAINTENANCE` \| `RETIRED` \| `DISPOSED`. |
| `notes` | `text` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | |

Indexes: `device(project_id)`, `device(lifecycle_status)`.

### `managed_service`

Domain name is `ManagedService` (not `Service`), to avoid confusion with
infrastructure/platform "services".

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `project_id` | `uuid` | FK → `project`, `ON DELETE RESTRICT`. |
| `host_device_id` | `uuid` | FK → `device`, `ON DELETE SET NULL`. A soft reference — see [ADR-0004](decisions/ADR-0004-service-dependency-behavior.md). |
| `name` | `varchar(200)` | Required. |
| `purpose` | `varchar(500)` | Nullable. |
| `description` | `text` | Nullable. |
| `status` | `varchar(20)` | `PLANNED` \| `INSTALLING` \| `CONFIGURING` \| `VALIDATING` \| `OPERATIONAL` \| `DEGRADED` \| `DISABLED` \| `RETIRED`. |
| `runtime_type` | `varchar(20)` | `DOCKER` \| `KUBERNETES` \| `VIRTUAL_MACHINE` \| `BARE_METAL` \| `MANAGED_CLOUD` \| `NAS_NATIVE` \| `OTHER`. |
| `storage_location` | `varchar(200)` | Nullable. |
| `sensitivity` | `varchar(20)` | `PUBLIC` \| `INTERNAL` \| `CONFIDENTIAL` \| `HIGHLY_SENSITIVE`. |
| `externally_exposed` | `boolean` | Required, default `false`. |
| `authentication_method` | `varchar(200)` | Nullable. |
| `backup_policy` | `varchar(500)` | Nullable; free text for now — a real FK to a `BackupPolicy` entity is a Milestone 5 concern. |
| `documentation_url`, `repository_url` | `varchar(2048)` | Nullable; validated as URLs when present. |
| `notes` | `text` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | |

Indexes: `managed_service(project_id)`, `managed_service(host_device_id)`,
`managed_service(status)`.

### `service_dependency`

Same shape as `task_dependency` (V2): `id`, `service_id` (FK →
`managed_service`, `ON DELETE CASCADE`), `depends_on_service_id` (FK →
`managed_service`, `ON DELETE CASCADE`), `created_at`. Constraints:
`service_id <> depends_on_service_id`; `UNIQUE (service_id,
depends_on_service_id)`. Cycle detection and the "can't delete a service
other services depend on" rule are application-layer — see
[ADR-0004](decisions/ADR-0004-service-dependency-behavior.md).

Indexes: `service_dependency(service_id)`,
`service_dependency(depends_on_service_id)`.

A project cannot be deleted while it has devices or services (same
restrictive pattern as phases and purchase items).

## V5 — backup and decisions (Milestone 5)

### `backup_policy`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `project_id` | `uuid` | FK → `project`, `ON DELETE RESTRICT`. |
| `name` | `varchar(200)` | Required. |
| `data_category` | `varchar(200)` | Required; free text (e.g. "Photos", "Service configs"). |
| `primary_location` | `varchar(200)` | Required. |
| `local_backup_location`, `offsite_backup_location` | `varchar(200)` | Nullable. |
| `encrypted` | `boolean` | Required, default `false`. |
| `contains_sensitive_data` | `boolean` | Required, default `false`. Not in the milestone's literal field list — added because the "missing encryption for sensitive off-site data" warning needs it. See [ADR-0005](decisions/ADR-0005-backup-coverage-rules.md). |
| `frequency` | `varchar(20)` | `CONTINUOUS` \| `HOURLY` \| `DAILY` \| `WEEKLY` \| `MONTHLY` \| `MANUAL`. |
| `retention`, `recovery_point_objective`, `recovery_time_objective` | `varchar` | Nullable; free text. |
| `last_verified_date` | `date` | Nullable. |
| `verification_notes` | `text` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | |

Note: there are no `coverage_state` / `missing_*` / `verification_overdue`
columns. All of them are computed at read time from the fields above.

Indexes: `backup_policy(project_id)`.

### `architecture_decision`

| Column | Type | Notes |
|---|---|---|
| `id` | `uuid` | PK, generated. |
| `project_id` | `uuid` | FK → `project`, `ON DELETE RESTRICT`. |
| `title` | `varchar(200)` | Required. |
| `status` | `varchar(20)` | `PROPOSED` \| `ACCEPTED` \| `DEPRECATED` \| `SUPERSEDED` \| `REJECTED`. |
| `context`, `alternatives_considered`, `consequences`, `revisit_criteria` | `text` | Nullable; plain multiline text (markdown-friendly by not being edited through a rich-text control). |
| `decision` | `text` | Required. |
| `decision_date` | `date` | Nullable. |
| `created_at`, `updated_at` | `timestamptz` | |

Indexes: `architecture_decision(project_id)`,
`architecture_decision(status)`.

### `decision_related_device`, `decision_related_service`

Plain many-to-many join tables (`decision_id` + `device_id` /
`service_id`, both `ON DELETE CASCADE`, composite PK) — an unordered
association with no extra attributes, so no dedicated entity class was
needed (unlike `task_dependency` / `service_dependency`, which carry
directionality and cycle constraints).

A project cannot be deleted while it has backup policies or architecture
decisions (same restrictive pattern as the rest of the domain).
