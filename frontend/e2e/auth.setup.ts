import { expect, test as setup } from '@playwright/test'
import { ADMIN_PASSWORD, ADMIN_USERNAME } from './credentials'

const authFile = 'e2e/.auth/admin.json'

setup('authenticate as the admin user', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel(/username/i).fill(ADMIN_USERNAME)
  await page.getByLabel(/password/i).fill(ADMIN_PASSWORD)
  await page.getByRole('button', { name: /sign in/i }).click()
  await expect(page).toHaveURL('/')
  await page.context().storageState({ path: authFile })
})
