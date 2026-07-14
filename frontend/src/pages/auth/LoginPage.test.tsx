import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../../auth/AuthContext'
import { LoginPage } from './LoginPage'

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

function problemResponse(detail: string, status: number): Response {
  return new Response(JSON.stringify({ title: 'Unauthorized', status, detail }), {
    status,
    headers: { 'Content-Type': 'application/problem+json' },
  })
}

function renderLoginPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<div>Home Page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('LoginPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows validation errors instead of submitting when fields are blank', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(problemResponse('Authentication is required.', 401))),
    )

    renderLoginPage()
    await screen.findByLabelText(/username/i)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText(/username is required/i)).toBeInTheDocument()
    expect(screen.getByText(/password is required/i)).toBeInTheDocument()
  })

  it('signs in with valid credentials and navigates away from /login', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input)
        if (url.includes('/auth/session')) {
          return Promise.resolve(problemResponse('Authentication is required.', 401))
        }
        if (url.includes('/auth/login') && init?.method === 'POST') {
          return Promise.resolve(jsonResponse({ username: 'admin' }))
        }
        return Promise.resolve(new Response(null, { status: 404 }))
      }),
    )

    renderLoginPage()
    await screen.findByLabelText(/username/i)

    const user = userEvent.setup()
    await user.type(screen.getByLabelText(/username/i), 'admin')
    await user.type(screen.getByLabelText(/password/i), 'correct-password')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText('Home Page')).toBeInTheDocument()
  })

  it('shows the server error message when credentials are rejected', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input)
        if (url.includes('/auth/session')) {
          return Promise.resolve(problemResponse('Authentication is required.', 401))
        }
        if (url.includes('/auth/login') && init?.method === 'POST') {
          return Promise.resolve(problemResponse('Invalid username or password.', 401))
        }
        return Promise.resolve(new Response(null, { status: 404 }))
      }),
    )

    renderLoginPage()
    await screen.findByLabelText(/username/i)

    const user = userEvent.setup()
    await user.type(screen.getByLabelText(/username/i), 'admin')
    await user.type(screen.getByLabelText(/password/i), 'wrong-password')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    expect(await screen.findByText(/invalid username or password/i)).toBeInTheDocument()
  })
})
