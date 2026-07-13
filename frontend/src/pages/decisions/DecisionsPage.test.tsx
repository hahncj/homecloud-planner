import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DecisionsPage } from './DecisionsPage'
import type { ArchitectureDecision } from '../../api/decisionTypes'
import type { Project } from '../../api/roadmapTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-13T00:00:00Z'

function buildProject(): Project {
  return {
    id: 'project-1',
    name: 'Personal Hybrid Cloud',
    description: null,
    status: 'IN_PROGRESS',
    budget: null,
    startDate: null,
    targetDate: null,
    createdAt: now,
    updatedAt: now,
    progress: { taskCount: 0, completedCount: 0, blockedCount: 0, progressPercentage: 0 },
  }
}

function buildDecisions(): ArchitectureDecision[] {
  return [
    {
      id: 'decision-1',
      projectId: 'project-1',
      title: 'Use a modular monolith',
      status: 'ACCEPTED',
      context: null,
      decision: 'Use one Spring Boot deployment organized by business feature.',
      alternativesConsidered: null,
      consequences: null,
      decisionDate: '2026-01-01',
      revisitCriteria: null,
      relatedDevices: [],
      relatedServices: [],
      createdAt: now,
      updatedAt: now,
    },
  ]
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderDecisionsPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <MemoryRouter>
          <DecisionsPage />
        </MemoryRouter>
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('DecisionsPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderDecisionsPage()

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

    renderDecisionsPage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('renders decision cards with status indicators', async () => {
    const project = buildProject()
    const decisions = buildDecisions()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/decisions')) return jsonResponse(decisions)
        if (url.includes('/devices')) return jsonResponse([])
        if (url.includes('/services')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderDecisionsPage()

    expect(await screen.findByText('Use a modular monolith')).toBeInTheDocument()
    expect(screen.getByText('ACCEPTED')).toBeInTheDocument()
  })

  it('shows an empty-filter message when no decisions match', async () => {
    const project = buildProject()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/decisions')) return jsonResponse([])
        if (url.includes('/devices')) return jsonResponse([])
        if (url.includes('/services')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderDecisionsPage()

    expect(await screen.findByText(/no decisions match the current filters/i)).toBeInTheDocument()
  })
})
