# ADR-0003: Budget-calculation behavior

## Status

Accepted

## Context

Milestone 3 adds a shopping list (`PurchaseItem`) and needs to answer, per
project: how much have we estimated we'll spend, how much have we actually
spent, how much are we committed to spending, and how much budget is left.
Purchase items move through a lifecycle (`IDEA` → `RESEARCHING` → `PLANNED`
→ `ORDERED` → `RECEIVED` → `INSTALLED`, or `CANCELLED` at any point) and
only some of them have a known actual price at any given time.

## Decision

- **Totals are calculated, never persisted**, mirroring the roadmap
  progress approach in [ADR-0002](ADR-0002-roadmap-domain-behavior.md).
  `estimatedTotal` and `actualTotal` on a `PurchaseItem` are always
  `unitPrice × quantity`, computed on read (`PurchaseItem.getEstimatedTotal
  / getActualTotal`). There is no stored total column to drift out of sync
  with a price or quantity edit.

- **Cancelled items are excluded from every budget figure** — estimated
  total, actual total, committed spending, and category summaries. A
  cancelled item was never going to be bought; including it would
  understate how much budget is actually available. This mirrors the
  roadmap rule that cancelled tasks are excluded from progress
  denominators.

- **Committed spending prefers the actual cost, falling back to the
  estimate.** For each non-cancelled item: use `actualTotal` if the actual
  unit price is known, otherwise use `estimatedTotal`, otherwise zero (an
  item with neither is not yet contributing a number, but it still exists
  as a placeholder in the list). This single figure answers "how much
  money is spoken for by this project" regardless of how far along each
  item is — an `IDEA`-stage item with only an estimate still represents
  planned spend, and a `RECEIVED` item's actual price is more accurate
  than its original estimate once it's known.

- **Remaining budget** is `project.budget - committedSpending`, or `null`
  when the project has no budget set (an unbounded project isn't "over" or
  "under" anything). `project.budget` itself is validated non-negative at
  the API layer (already enforced by `ProjectRequest` since Milestone 2);
  this ADR does not change that rule, just documents that budget
  calculations rely on it holding.

- **Category summaries** group the same non-cancelled items by their
  free-text `category` field and report the same four figures
  (`itemCount`, `estimatedTotal`, `actualTotal`, `committedSpending`) per
  category, so the shopping list UI can show a breakdown without a second,
  differently-defined calculation path. `category` is free text rather
  than a fixed enum — a personal hybrid-cloud project's natural categories
  (networking, compute, storage, cabling, licensing, ...) aren't fixed
  ahead of time, and the UI derives its category filter options from
  whatever values are actually in use.

## Alternatives considered

- Always using the estimate until an item is fully `RECEIVED`. Rejected:
  once an actual price is known (e.g. at `ORDERED` after checkout), it's a
  strictly better number than the original estimate, and there's no reason
  to keep reporting the stale estimate until delivery.
- Summing estimated and actual totals separately and letting the frontend
  decide which to treat as "committed." Rejected: every consumer of this
  number (summary cards, category rows, future dashboard) would have to
  reimplement the same actual-then-estimate preference, which is exactly
  the kind of duplicated business rule the backend should own.

## Consequences

Every budget read recomputes totals from the current purchase-item list —
O(items) per request, negligible at this application's scale. The numbers
shown are always consistent with the latest prices, quantities, and
statuses with no cache-invalidation risk.

## Revisit criteria

Revisit if a future milestone needs historical budget tracking (e.g. "what
did we think we'd spend last month") — that requires persisting snapshots,
which this ADR deliberately avoids for the current numbers.
