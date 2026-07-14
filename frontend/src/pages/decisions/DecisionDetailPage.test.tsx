import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DecisionDetailPage } from './DecisionDetailPage'
import type { ArchitectureDecision } from '../../api/decisionTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-13T00:00:00Z'

function buildDecision(): ArchitectureDecision {
  return {
    id: 'decision-1',
    projectId: 'project-1',
    title: 'Use a modular monolith',
    status: 'ACCEPTED',
    context: 'We need something simple to deploy.',
    decision: 'Use one Spring Boot deployment organized by business feature.',
    alternativesConsidered: 'Microservices; a full-stack JS app.',
    consequences: 'Simple to deploy, boundaries preserved internally.',
    decisionDate: '2026-01-01',
    revisitCriteria: 'Revisit if a module needs independent scaling.',
    relatedDevices: [{ id: 'device-1', name: 'NAS' }],
    relatedServices: [{ id: 'service-1', name: 'Plex' }],
    createdAt: now,
    updatedAt: now,
  }
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderDecisionDetailPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <MemoryRouter initialEntries={['/decisions/decision-1']}>
          <Routes>
            <Route path="/decisions/:decisionId" element={<DecisionDetailPage />} />
          </Routes>
        </MemoryRouter>
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('DecisionDetailPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while the decision is being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderDecisionDetailPage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading decision/i)
  })

  it('shows an error state when the decision fails to load', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(new Response(null, { status: 404 }))),
    )

    renderDecisionDetailPage()

    expect(await screen.findByText(/unable to load this decision/i)).toBeInTheDocument()
  })

  it('renders decision content and related devices/services', async () => {
    const decision = buildDecision()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/decisions/decision-1')) return jsonResponse(decision)
        if (url.includes('/decisions')) return jsonResponse([decision])
        if (url.includes('/devices')) return jsonResponse([])
        if (url.includes('/services')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderDecisionDetailPage()

    expect(await screen.findByRole('heading', { name: 'Use a modular monolith' })).toBeInTheDocument()
    expect(screen.getByText('Use one Spring Boot deployment organized by business feature.')).toBeInTheDocument()
    expect(screen.getByText('NAS')).toBeInTheDocument()
    expect(screen.getByText('Plex')).toBeInTheDocument()
  })
})
