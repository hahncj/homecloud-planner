# End-to-end tests

Playwright specs that drive the real app in a browser against an
already-running stack — there's no seed/reset endpoint outside the `dev`
profile (see ADR-0006) and only one admin account, so these tests don't spin
up or tear down their own environment.

## Running

1. Start the stack (from the repo root) and make sure `.env` has
   `ADMIN_USERNAME`/`ADMIN_PASSWORD` set:

   ```bash
   docker compose up -d
   ```

2. From `frontend/`:

   ```bash
   npm run e2e
   ```

   By default this targets `http://localhost:3000` and reads
   `ADMIN_USERNAME`/`ADMIN_PASSWORD` from the process environment, falling
   back to the repo-root `.env` file. Override the target with
   `PLAYWRIGHT_BASE_URL` if the frontend is running somewhere else (e.g. the
   Vite dev server on `http://localhost:5173`).

## Layout

- `credentials.ts` — resolves the admin username/password the suite logs in
  with.
- `auth.setup.ts` — a Playwright "setup" project that logs in once and saves
  the resulting session cookie to `e2e/.auth/admin.json` (gitignored), reused
  by every other spec via `storageState` so they don't each re-run the login
  form.
- `login.spec.ts` — runs unauthenticated: redirect-to-login, invalid
  credentials, and a successful sign-in.
- `planner-workflow.spec.ts` — one project built up across serial tests in
  the order a user would work through it (create project → add phase → add
  task → add dependency → confirm blocked → add purchase → add device → add
  service → add backup policy → view dashboard → export project). Each test
  gets its own page, so state that lives only in React context (which
  project is selected) is explicitly re-established via the UI rather than
  assumed to carry over.

Re-running the suite creates a new project each time (the name includes a
timestamp) rather than resetting the database, so old E2E projects will
accumulate in a long-lived dev database — harmless, but worth an occasional
manual cleanup.
