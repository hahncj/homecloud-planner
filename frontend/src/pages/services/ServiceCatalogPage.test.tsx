import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ServiceCatalogPage } from './ServiceCatalogPage'
import type { Project } from '../../api/roadmapTypes'
import type { ManagedService } from '../../api/serviceCatalogTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-12T00:00:00Z'

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

function buildServices(): ManagedService[] {
  return [
    {
      id: 'service-1',
      projectId: 'project-1',
      hostDeviceId: null,
      name: 'Reverse Proxy',
      purpose: 'Routes traffic to internal services',
      description: null,
      status: 'OPERATIONAL',
      runtimeType: 'DOCKER',
      storageLocation: null,
      sensitivity: 'INTERNAL',
      externallyExposed: true,
      authenticationMethod: null,
      backupPolicy: null,
      documentationUrl: null,
      repositoryUrl: null,
      notes: null,
      dependsOnServiceIds: [],
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

function renderServiceCatalogPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <MemoryRouter>
          <ServiceCatalogPage />
        </MemoryRouter>
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('ServiceCatalogPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderServiceCatalogPage()

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

    renderServiceCatalogPage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('renders services with status, runtime, and exposure indicators', async () => {
    const project = buildProject()
    const services = buildServices()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/services')) return jsonResponse(services)
        if (url.includes('/devices')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderServiceCatalogPage()

    expect(await screen.findByText('Reverse Proxy')).toBeInTheDocument()
    expect(screen.getByText('OPERATIONAL')).toBeInTheDocument()
    expect(screen.getByText('External')).toBeInTheDocument()
  })

  it('shows an empty-filter message when no services match', async () => {
    const project = buildProject()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/services')) return jsonResponse([])
        if (url.includes('/devices')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderServiceCatalogPage()

    expect(await screen.findByText(/no services match the current filters/i)).toBeInTheDocument()
  })
})
