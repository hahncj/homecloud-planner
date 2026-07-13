# ADR-0005: Backup coverage rules

## Status

Accepted

## Context

Milestone 5 adds `BackupPolicy` records — one per data category (photos,
documents, service configs, etc.) — that describe where that category's
primary copy lives and whether it has local and/or off-site backups. The
backup matrix needs to surface, at a glance, which data categories are
under-protected, without the user having to read every field of every
policy.

## Decision

- **Coverage, warnings, and verification state are calculated, never
  persisted**, following the same pattern as roadmap progress
  ([ADR-0002](ADR-0002-roadmap-domain-behavior.md)) and budget figures
  ([ADR-0003](ADR-0003-budget-calculation-behavior.md)). `BackupCoverageState`
  (`NONE` / `PARTIAL` / `FULL`) is derived from whether
  `localBackupLocation` and `offsiteBackupLocation` are present — nothing
  about coverage is stored as its own column.

- **Four warning signals**, each a simple, independently-checkable rule:
  - *Missing local backup*: `localBackupLocation` is blank.
  - *Missing off-site backup*: `offsiteBackupLocation` is blank.
  - *Missing encryption for sensitive off-site data*: the policy is
    flagged `containsSensitiveData`, has an off-site location, and
    `encrypted` is `false`. This warning is deliberately narrow — it does
    not fire for sensitive data with no off-site copy at all (that's
    already covered by the missing-off-site warning) or for non-sensitive
    data (unencrypted off-site storage of non-sensitive data is a
    reasonable choice, not a warning-worthy one).
  - *Verification overdue*: `lastVerifiedDate` is more than 90 days ago,
    or was never set. A backup nobody has confirmed actually restores is
    not meaningfully different from no backup — the 90-day threshold is a
    reasonable default cadence for a personal/home-lab setup and is a
    named constant (`BackupCoverageCalculator.VERIFICATION_OVERDUE_AFTER_DAYS`)
    rather than a magic number, so it's easy to find and revisit.

- **`containsSensitiveData` is a field on `BackupPolicy`.** It isn't in
  the milestone's literal field list, but the milestone explicitly
  requires "missing encryption warning for sensitive off-site data," and
  there was no other field in the model this could be derived from —
  sensitivity is a concept that exists on `ManagedService` (Milestone 4)
  but a backup policy covers a data *category*, not a specific service.
  Adding the boolean was the minimal change needed to implement a
  requirement that was already explicitly specified.

- **RAID and snapshots are not backups, and the UI says so.** The Backup
  Matrix page carries a persistent disclaimer: RAID and filesystem
  snapshots protect against drive failure, not against deletion,
  ransomware, fire, or theft, and don't count as a backup on their own.
  This is a product-copy decision, not a data-model one — there's no
  "RAID" or "snapshot" field to validate against, because the point is
  that those mechanisms are out of scope for what this matrix tracks at
  all. A `localBackupLocation` pointing at "the same NAS, different RAID
  array" is technically fillable but does not protect against the NAS
  itself failing or being compromised; the tool cannot detect that
  semantic mistake, so it states the rule in the UI instead.

- **`backupPolicy` on `ManagedService` (Milestone 4) stays free text.**
  Now that a real `BackupPolicy` entity exists, linking a service to the
  specific policy that covers it is tempting, but a service's backup
  posture is really "which data category(ies) does this service's data
  belong to," which isn't a clean one-to-one relationship (a service can
  touch multiple categories; a category can span multiple services). That
  mapping is a real feature but a separate one from what this milestone
  asked for, so it was left as-is rather than retrofitting Milestone 4's
  schema.

## Alternatives considered

- A single "backup health" score per category instead of four independent
  warnings. Rejected: a blended score hides *which* specific thing is
  wrong, and the four rules are each independently actionable.
- Making the verification-overdue threshold user-configurable per
  project. Rejected as unnecessary complexity for now — a single sensible
  default, named and easy to change in code, is enough until there's a
  demonstrated need for per-project tuning.

## Consequences

Every backup-policy read recomputes coverage and warnings — O(1) per
policy, negligible at this application's scale — and the numbers always
reflect the latest fields with no persisted state to drift out of sync.

## Revisit criteria

Revisit the 90-day threshold or the service-to-category mapping if real
usage shows the default doesn't fit, or if a future milestone (e.g. the
dashboard's backup-coverage warnings) needs a per-service view of backup
coverage rather than a per-category one.
