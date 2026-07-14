import { defineConfig, devices } from '@playwright/test'

const AUTH_FILE = 'e2e/.auth/admin.json'

// Exercises the real, already-running stack (docker compose, or `npm run
// dev` + a locally running backend) rather than spinning up its own — this
// is a local-network app with a single admin account and no seed/reset
// endpoint outside the dev profile, so there's no throwaway environment for
// Playwright to own. See e2e/README.md for how to run it.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: 'list',
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL ?? 'http://localhost:3000',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'unauthenticated',
      testMatch: /login\.spec\.ts/,
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'authenticated',
      testIgnore: /login\.spec\.ts/,
      testMatch: /.*\.spec\.ts/,
      use: { ...devices['Desktop Chrome'], storageState: AUTH_FILE },
      dependencies: ['setup'],
    },
  ],
})
