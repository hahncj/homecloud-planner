import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { RoadmapPage } from './RoadmapPage'
import type { Project, Roadmap } from '../../api/roadmapTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-12T00:00:00Z'

function buildProject(overrides: Partial<Project> = {}): Project {
  return {
    id: 'project-1',
    name: 'Personal Hybrid Cloud',
    description: null,
    status: 'IN_PROGRESS',
    budget: '5000.00',
    startDate: '2026-01-01',
    targetDate: '2026-12-31',
    createdAt: now,
    updatedAt: now,
    progress: { taskCount: 2, completedCount: 1, blockedCount: 1, progressPercentage: 50 },
    ...overrides,
  }
}

function buildRoadmap(): Roadmap {
  const project = buildProject()
  return {
    project,
    phases: [
      {
        id: 'phase-1',
        projectId: project.id,
        name: 'Foundation',
        description: null,
        sequence: 1,
        createdAt: now,
        updatedAt: now,
        progress: { taskCount: 2, completedCount: 1, blockedCount: 1, progressPercentage: 50 },
      },
    ],
    tasks: [
      {
        id: 'task-1',
        phaseId: 'phase-1',
        projectId: project.id,
        title: 'Rack the switch',
        description: null,
        status: 'COMPLETED',
        priority: 'HIGH',
        estimatedCost: null,
        actualCost: null,
        targetDate: null,
        completedDate: null,
        acceptanceCriteria: null,
        notes: null,
        blocked: false,
        dependsOnTaskIds: [],
        createdAt: now,
        updatedAt: now,
      },
      {
        id: 'task-2',
        phaseId: 'phase-1',
        projectId: project.id,
        title: 'Configure VLANs',
        description: null,
        status: 'NOT_STARTED',
        priority: 'MEDIUM',
        estimatedCost: null,
        actualCost: null,
        targetDate: null,
        completedDate: null,
        acceptanceCriteria: null,
        notes: null,
        blocked: true,
        dependsOnTaskIds: ['task-1'],
        createdAt: now,
        updatedAt: now,
      },
    ],
  }
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderRoadmapPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <RoadmapPage />
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('RoadmapPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderRoadmapPage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading projects/i)
  })

  it('shows an error state when projects fail to load', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(new Response(null, { status: 500 }))),
    )

    renderRoadmapPage()

    expect(await screen.findByText(/unable to load projects/i)).toBeInTheDocument()
  })

  it('shows an empty state when there are no projects', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/projects')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderRoadmapPage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('renders project summary, phase progress, and blocked tasks once loaded', async () => {
    const project = buildProject()
    const roadmap = buildRoadmap()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/roadmap')) return jsonResponse(roadmap)
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderRoadmapPage()

    expect(await screen.findByText('Rack the switch')).toBeInTheDocument()
    expect(screen.getByText('Configure VLANs')).toBeInTheDocument()
    expect(screen.getAllByText(/blocked/i).length).toBeGreaterThan(0)

    const phaseCard = screen.getByText('1. Foundation').closest('div')
    expect(phaseCard).not.toBeNull()
  })

  it('switches to the table view and lists tasks with their phase', async () => {
    const project = buildProject()
    const roadmap = buildRoadmap()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/roadmap')) return jsonResponse(roadmap)
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderRoadmapPage()
    await screen.findByText('Rack the switch')

    const user = userEvent.setup()
    await user.click(screen.getByRole('tab', { name: /table/i }))

    const table = await screen.findByRole('table', { name: /tasks/i })
    expect(within(table).getByText('Rack the switch')).toBeInTheDocument()
    expect(within(table).getAllByText('Foundation')).toHaveLength(2)
  })

  it('validates required fields when creating a project', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.endsWith('/projects')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderRoadmapPage()
    await screen.findByText(/no projects yet/i)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /new project/i }))
    await user.click(screen.getByRole('button', { name: /^save$/i }))

    await waitFor(() => {
      expect(screen.getByText(/name is required/i)).toBeInTheDocument()
    })
  })
})
