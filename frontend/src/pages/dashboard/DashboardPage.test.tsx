import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DashboardPage } from './DashboardPage'
import type { DashboardSummary } from '../../api/dashboardTypes'
import type { Project } from '../../api/roadmapTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-13T00:00:00Z'

function buildProject(): Project {
  return {
    id: 'project-1',
    name: 'Personal Hybrid Cloud',
    description: null,
    status: 'IN_PROGRESS',
    budget: '6000.00',
    startDate: null,
    targetDate: null,
    createdAt: now,
    updatedAt: now,
    progress: { taskCount: 4, completedCount: 1, blockedCount: 1, progressPercentage: 25 },
  }
}

function buildDashboard(): DashboardSummary {
  return {
    projectId: 'project-1',
    projectName: 'Personal Hybrid Cloud',
    overallProgress: { taskCount: 4, completedCount: 1, blockedCount: 1, progressPercentage: 25 },
    currentPhase: {
      id: 'phase-1',
      name: 'Foundation',
      sequence: 1,
      progress: { taskCount: 2, completedCount: 1, blockedCount: 1, progressPercentage: 50 },
    },
    totalBudget: '6000.00',
    estimatedSpending: '1500.00',
    actualSpending: '400.00',
    remainingBudget: '5600.00',
    blockedTaskCount: 1,
    blockedTasks: [
      {
        id: 'task-1',
        title: 'Configure NAS',
        phaseId: 'phase-1',
        phaseName: 'Foundation',
        status: 'NOT_STARTED',
        priority: 'HIGH',
        targetDate: null,
        completedDate: null,
        blocked: true,
      },
    ],
    upcomingTargetDates: [],
    recentCompletedTasks: [],
    purchaseStatusCounts: { PLANNED: 2 },
    deviceLifecycleCounts: { ACTIVE: 3 },
    serviceStatusCounts: { PLANNED: 5 },
    backupCoverageWarnings: {
      missingLocalBackupCount: 1,
      missingOffsiteBackupCount: 0,
      missingEncryptionForSensitiveOffsiteCount: 0,
      verificationOverdueCount: 2,
    },
    upcomingWarrantyExpirations: [],
    nextRecommendedActions: [{ category: 'BLOCKING_TASK', message: "Finish 'Select NAS' so 'Configure NAS' can start." }],
  }
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderDashboardPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <DashboardPage />
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('DashboardPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderDashboardPage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading projects/i)
  })

  it('shows an empty state when there are no projects', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.endsWith('/projects')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderDashboardPage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('renders progress, budget, blocked tasks, and recommended actions', async () => {
    const project = buildProject()
    const dashboard = buildDashboard()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/dashboard')) return jsonResponse(dashboard)
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderDashboardPage()

    expect(await screen.findByText('Configure NAS')).toBeInTheDocument()
    expect(screen.getByText(/Finish 'Select NAS' so 'Configure NAS' can start\./)).toBeInTheDocument()
    expect(screen.getByText('$5,600.00')).toBeInTheDocument()
    expect(screen.getByText(/Missing local backup: 1/)).toBeInTheDocument()
    expect(screen.getByText(/PLANNED: 2/)).toBeInTheDocument()
  })
})
