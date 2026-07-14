import { expect, test } from '@playwright/test'
import type { Page } from '@playwright/test'

// Each Playwright test gets its own fresh page (and therefore a fresh
// in-memory SelectedProjectContext, which isn't persisted anywhere) even
// within a single describe.serial block, so — unlike a real user staying on
// one tab — every test has to explicitly reselect the project it's working
// with after navigating.
async function selectProject(page: Page, projectName: string) {
  await page.getByRole('combobox', { name: 'Project' }).click()
  await page.getByRole('option', { name: projectName }).click()
}

// One project, built up across serial tests in the order a real user would
// work through it — each flow depends on state the previous one created
// (a phase needs a project, a dependency needs two tasks, ...), so this
// mirrors the "12 named flows" checklist as one continuous journey rather
// than 12 independent fixtures that would each have to re-derive that state.
//
// Every field is filled via a `dialog` locator scoped to
// page.getByRole('dialog'), not the bare page: most of these pages have a
// filter bar hidden behind the open dialog with fields sharing the same
// label ("Category", "Status", ...), which makes an unscoped getByLabel
// ambiguous.
test.describe.serial('planner workflow', () => {
  const projectName = `E2E Project ${Date.now()}`
  const phaseName = 'Foundation'
  const prerequisiteTaskTitle = 'Select NAS hardware'
  const dependentTaskTitle = 'Configure NAS'

  test('create project', async ({ page }) => {
    await page.goto('/roadmap')
    await page.getByRole('button', { name: 'New project' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(/^Name/).fill(projectName)
    await dialog.getByRole('button', { name: 'Save' }).click()

    // Newly created projects are auto-selected in the page that created
    // them, so the phase button for it should already be live.
    await expect(page.getByRole('button', { name: 'New phase' })).toBeVisible()
  })

  test('add phase', async ({ page }) => {
    await page.goto('/roadmap')
    await selectProject(page, projectName)

    await page.getByRole('button', { name: 'New phase' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(/^Name/).fill(phaseName)
    await dialog.getByRole('button', { name: 'Save' }).click()

    await expect(page.getByText(`1. ${phaseName}`)).toBeVisible()
  })

  test('add task', async ({ page }) => {
    await page.goto('/roadmap')
    await selectProject(page, projectName)

    await page.getByRole('button', { name: `Add task to ${phaseName}` }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel('Title').fill(prerequisiteTaskTitle)
    await dialog.getByRole('button', { name: 'Save' }).click()

    await expect(page.getByText(prerequisiteTaskTitle)).toBeVisible()
  })

  test('add dependency', async ({ page }) => {
    await page.goto('/roadmap')
    await selectProject(page, projectName)

    // A second task for the first one to depend on.
    await page.getByRole('button', { name: `Add task to ${phaseName}` }).click()
    let dialog = page.getByRole('dialog')
    await dialog.getByLabel('Title').fill(dependentTaskTitle)
    await dialog.getByRole('button', { name: 'Save' }).click()
    await expect(page.getByText(dependentTaskTitle)).toBeVisible()

    // Reopen it in edit mode — the "Depends on" section only renders for an
    // existing task, not while creating one — and add the dependency.
    await page.getByRole('button', { name: `Edit ${dependentTaskTitle}` }).click()
    dialog = page.getByRole('dialog')
    await dialog.getByRole('combobox', { name: 'Add dependency' }).click()
    await page.getByRole('option', { name: prerequisiteTaskTitle }).click()
    await dialog.getByRole('button', { name: 'Add dependency' }).click()
    await expect(dialog.getByText(prerequisiteTaskTitle)).toBeVisible()
    await dialog.getByRole('button', { name: 'Cancel' }).click()
  })

  test('confirm blocked behavior', async ({ page }) => {
    await page.goto('/roadmap')
    await selectProject(page, projectName)

    const dependentCard = page.getByRole('button', { name: `Edit ${dependentTaskTitle}` })
    await expect(dependentCard.getByText('Blocked')).toBeVisible()

    const prerequisiteCard = page.getByRole('button', { name: `Edit ${prerequisiteTaskTitle}` })
    await expect(prerequisiteCard.getByText('Blocked')).toHaveCount(0)
  })

  test('add purchase', async ({ page }) => {
    await page.goto('/shopping')
    await selectProject(page, projectName)

    await page.getByRole('button', { name: 'New purchase item' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel('Category').fill('Networking')
    await dialog.getByLabel('Product name').fill('24-port managed switch')
    await dialog.getByRole('button', { name: 'Save' }).click()

    await expect(page.getByText('24-port managed switch')).toBeVisible()
  })

  test('add device', async ({ page }) => {
    await page.goto('/hardware')
    await selectProject(page, projectName)

    await page.getByRole('button', { name: 'New device' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(/^Name/).fill('Primary NAS')
    await dialog.getByRole('button', { name: 'Save' }).click()

    await expect(page.getByText('Primary NAS')).toBeVisible()
  })

  test('add service', async ({ page }) => {
    await page.goto('/services')
    await selectProject(page, projectName)

    await page.getByRole('button', { name: 'New service' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(/^Name/).fill('Plex Media Server')
    await dialog.getByRole('button', { name: 'Save' }).click()

    await expect(page.getByText('Plex Media Server')).toBeVisible()
  })

  test('add backup policy', async ({ page }) => {
    await page.goto('/backup')
    await selectProject(page, projectName)

    await page.getByRole('button', { name: 'New backup policy' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(/^Name/).fill('Photo library backup')
    await dialog.getByLabel('Data category').fill('Photos')
    await dialog.getByLabel('Primary location').fill('NAS volume1')
    await dialog.getByRole('button', { name: 'Save' }).click()

    await expect(page.getByText('Photo library backup')).toBeVisible()
  })

  test('view dashboard', async ({ page }) => {
    await page.goto('/')
    await selectProject(page, projectName)

    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
    // Confirms the dashboard's aggregation reflects the data built up by
    // the flows above (the blocked task from the dependency flow), not
    // just that the page renders.
    await expect(page.getByRole('heading', { name: 'Blocked tasks' })).toBeVisible()
    await expect(page.getByText(dependentTaskTitle, { exact: true })).toBeVisible()
  })

  test('export project', async ({ page }) => {
    await page.goto('/settings')

    const downloadPromise = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Download JSON' }).click()
    const download = await downloadPromise

    expect(download.suggestedFilename()).toBe('homecloud-planner-export.json')
  })
})
