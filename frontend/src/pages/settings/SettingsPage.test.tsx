import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { SettingsPage } from './SettingsPage'

function renderSettingsPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SettingsPage />
    </QueryClientProvider>,
  )
}

describe('SettingsPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while checking backend health', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderSettingsPage()

    expect(screen.getByRole('status')).toHaveTextContent(/checking backend health/i)
  })

  it('shows the backend status once the health check responds', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ status: 'UP', timestamp: '2026-07-13T10:00:00Z' }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
          }),
        ),
      ),
    )

    renderSettingsPage()

    expect(await screen.findByText(/backend is UP/i)).toBeInTheDocument()
  })

  it('shows an error when the JSON export download fails', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/health')) {
          return Promise.resolve(
            new Response(JSON.stringify({ status: 'UP', timestamp: '2026-07-13T10:00:00Z' }), {
              status: 200,
              headers: { 'Content-Type': 'application/json' },
            }),
          )
        }
        return Promise.resolve(new Response(null, { status: 500 }))
      }),
    )

    renderSettingsPage()
    await screen.findByText(/backend is UP/i)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /download json/i }))

    expect(await screen.findByText(/export request to \/export\/json failed/i)).toBeInTheDocument()
  })
})
