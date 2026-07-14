# Backing up and restoring the Planner's own database

HomeCloud Planner helps you track backups for *your other* systems (the
backup matrix — see [ADR-0005](decisions/ADR-0005-backup-coverage-rules.md))
but doesn't back up its own data automatically. This is the manual
procedure for its PostgreSQL database, which is the sole system of record —
there is no other copy of your project data anywhere.

## What you're backing up

Everything lives in the `homecloud-postgres` Docker volume, managed by the
`postgres` service in `compose.yaml`. A `pg_dump` of the `homecloud`
database (or whatever `POSTGRES_DB` is set to in your `.env`) captures all
of it: projects, phases, tasks and dependencies, purchase items, devices,
managed services, backup policies, architecture decisions, and the admin
account's hashed password.

## Backup

With the stack running (`docker compose up -d`):

```bash
docker compose exec postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -F c -f /tmp/homecloud-backup.dump
docker compose cp postgres:/tmp/homecloud-backup.dump ./homecloud-backup-$(date +%Y%m%d).dump
docker compose exec postgres rm /tmp/homecloud-backup.dump
```

`-F c` uses PostgreSQL's custom archive format (compressed, and restorable
selectively with `pg_restore`), which is what the restore instructions
below assume. Store the resulting `.dump` file somewhere other than the
same disk as the Docker volume — a dump sitting next to the thing it's a
backup of doesn't survive that disk failing.

For a quick one-off snapshot instead of piping through a temp file, plain
SQL text also works and is easier to eyeball:

```bash
docker compose exec postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" > homecloud-backup-$(date +%Y%m%d).sql
```

## Restore

**Restoring overwrites existing data — confirm you're pointed at the
database you intend to replace before running this.**

Against a running stack, into the existing (possibly non-empty) database:

```bash
docker compose cp ./homecloud-backup-20260712.dump postgres:/tmp/restore.dump
docker compose exec postgres pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists /tmp/restore.dump
docker compose exec postgres rm /tmp/restore.dump
```

`--clean --if-exists` drops existing objects before recreating them from
the dump, so the result matches the dump exactly rather than merging with
whatever was already there.

For a plain-SQL dump taken with the second method above:

```bash
docker compose cp ./homecloud-backup-20260712.sql postgres:/tmp/restore.sql
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /tmp/restore.sql'
docker compose exec postgres rm /tmp/restore.sql
```

### Restoring into a fresh volume

If you're rebuilding the stack from scratch (e.g. new hardware), start
Postgres alone first so the volume and empty database exist, then restore
before starting the backend (so Flyway doesn't race a restore that's still
in progress):

```bash
docker compose up -d postgres
# wait for it to report healthy: docker compose ps
docker compose cp ./homecloud-backup-20260712.dump postgres:/tmp/restore.dump
docker compose exec postgres pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists /tmp/restore.dump
docker compose up -d
```

## Verifying a backup is actually restorable

A backup you've never test-restored is a hope, not a backup — the same
principle this app's own backup matrix nags you about for your other
systems applies to it too. Periodically restore a dump into a scratch
database and sanity-check it:

```bash
docker compose exec postgres createdb -U "$POSTGRES_USER" homecloud_verify
docker compose cp ./homecloud-backup-20260712.dump postgres:/tmp/verify.dump
docker compose exec postgres pg_restore -U "$POSTGRES_USER" -d homecloud_verify /tmp/verify.dump
docker compose exec postgres psql -U "$POSTGRES_USER" -d homecloud_verify -c 'select count(*) from project;'
docker compose exec postgres dropdb -U "$POSTGRES_USER" homecloud_verify
docker compose exec postgres rm /tmp/verify.dump
```

## What a database backup does *not* cover

- The admin account's plaintext password (only its BCrypt hash is in the
  dump — if you've lost the plaintext, that's fine, you still log in with
  it; but if you're provisioning a *new* deployment from a dump, you still
  need to set `ADMIN_USERNAME`/`ADMIN_PASSWORD` in that deployment's
  `.env`, since `AdminAccountInitializer` only acts when the table is
  empty and a restored dump's table won't be).
- `.env` itself, or any other environment configuration — back that up
  separately (outside of git, since it holds real credentials).
- Anything the export feature produces (`docs/decisions/ADR-0007-export-
  format.md`'s JSON/Markdown output) is a point-in-time human-readable
  snapshot for sharing or archival, not a substitute for this — it isn't
  designed to be restored from.
