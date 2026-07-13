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
