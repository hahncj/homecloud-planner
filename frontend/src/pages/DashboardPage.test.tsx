import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DashboardPage } from './DashboardPage'

function renderDashboard() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <DashboardPage />
    </QueryClientProvider>,
  )
}

describe('DashboardPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while the health check is in flight', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderDashboard()

    expect(screen.getByRole('status')).toHaveTextContent(/checking backend health/i)
  })

  it('shows the UP status once the backend responds successfully', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ status: 'UP', timestamp: '2026-07-12T10:00:00Z' }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          }),
        ),
      ),
    )

    renderDashboard()

    expect(await screen.findByText(/backend is UP/i)).toBeInTheDocument()
  })

  it('shows an error state when the health check fails', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(new Response(null, { status: 503 }))),
    )

    renderDashboard()

    expect(await screen.findByText(/unable to reach the backend/i)).toBeInTheDocument()
  })
})
