import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ServiceDetailPage } from './ServiceDetailPage'
import type { ManagedService } from '../../api/serviceCatalogTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-12T00:00:00Z'

function buildService(overrides: Partial<ManagedService> = {}): ManagedService {
  return {
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
    ...overrides,
  }
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderServiceDetailPage(serviceId = 'service-1') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <MemoryRouter initialEntries={[`/services/${serviceId}`]}>
          <Routes>
            <Route path="/services/:serviceId" element={<ServiceDetailPage />} />
          </Routes>
        </MemoryRouter>
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('ServiceDetailPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while the service is being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderServiceDetailPage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading service/i)
  })

  it('shows an error state when the service fails to load', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(new Response(null, { status: 404 }))),
    )

    renderServiceDetailPage()

    expect(await screen.findByText(/unable to load this service/i)).toBeInTheDocument()
  })

  it('renders service details, indicators, and the dependency section', async () => {
    const service = buildService()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/services/service-1')) return jsonResponse(service)
        if (url.includes('/services')) return jsonResponse([service])
        if (url.includes('/devices')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderServiceDetailPage()

    expect(await screen.findByRole('heading', { name: 'Reverse Proxy' })).toBeInTheDocument()
    expect(screen.getByText('OPERATIONAL')).toBeInTheDocument()
    expect(screen.getByText('Externally exposed')).toBeInTheDocument()
    expect(screen.getByText(/no dependencies/i)).toBeInTheDocument()
  })
})
