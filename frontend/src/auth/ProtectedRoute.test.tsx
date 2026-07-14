import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from './AuthContext'
import { ProtectedRoute } from './ProtectedRoute'

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

function problemResponse(status: number): Response {
  return new Response(JSON.stringify({ title: 'Unauthorized', status, detail: 'Authentication is required.' }), {
    status,
    headers: { 'Content-Type': 'application/problem+json' },
  })
}

function renderProtectedRoute() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/roadmap']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>Login Page</div>} />
            <Route element={<ProtectedRoute />}>
              <Route path="/roadmap" element={<div>Roadmap Page</div>} />
            </Route>
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('ProtectedRoute', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('redirects to /login when there is no session', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(problemResponse(401))),
    )

    renderProtectedRoute()

    expect(await screen.findByText('Login Page')).toBeInTheDocument()
  })

  it('renders the protected content when a session exists', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(jsonResponse({ username: 'admin' }))),
    )

    renderProtectedRoute()

    expect(await screen.findByText('Roadmap Page')).toBeInTheDocument()
  })
})
