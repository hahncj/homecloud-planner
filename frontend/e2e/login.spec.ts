import { expect, test } from '@playwright/test'
import { ADMIN_PASSWORD, ADMIN_USERNAME } from './credentials'

// Runs unauthenticated (no storageState) — see the "unauthenticated"
// Playwright project in playwright.config.ts.
test.describe('login', () => {
  test('redirects an unauthenticated visitor to /login', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL('/login')
  })

  test('rejects an invalid password with an error message', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel(/username/i).fill(ADMIN_USERNAME)
    await page.getByLabel(/password/i).fill('definitely-wrong-password')
    await page.getByRole('button', { name: /sign in/i }).click()

    await expect(page.getByText(/invalid username or password/i)).toBeVisible()
    await expect(page).toHaveURL('/login')
  })

  test('signs in with valid credentials and reaches the dashboard', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel(/username/i).fill(ADMIN_USERNAME)
    await page.getByLabel(/password/i).fill(ADMIN_PASSWORD)
    await page.getByRole('button', { name: /sign in/i }).click()

    await expect(page).toHaveURL('/')
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  })
})
