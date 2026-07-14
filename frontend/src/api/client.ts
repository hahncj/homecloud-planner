export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL

export interface ProblemDetail {
  title?: string
  status?: number
  detail?: string
}

export class ApiError extends Error {
  readonly status: number
  readonly problem: ProblemDetail | undefined

  constructor(message: string, status: number, problem?: ProblemDetail) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

// Set by AuthProvider so a 401 from any request (not just the session
// check) can clear cached auth state and send the user back to /login —
// e.g. when a session expires mid-use. Kept as a plain module-level hook
// rather than a context import so this stays a dependency-free fetch
// wrapper usable outside React.
let unauthorizedHandler: (() => void) | undefined

export function setUnauthorizedHandler(handler: (() => void) | undefined): void {
  unauthorizedHandler = handler
}

function readCookie(name: string): string | undefined {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`))
  return match?.[1] ? decodeURIComponent(match[1]) : undefined
}

async function parseProblem(response: Response): Promise<ProblemDetail | undefined> {
  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('json')) {
    return undefined
  }
  try {
    return (await response.json()) as ProblemDetail
  } catch {
    return undefined
  }
}

// The session-check itself 401s any time nobody is logged in — that's the
// expected, steady-state result, not a session expiring. Routing it through
// unauthorizedHandler too would reset the very query that's mid-fetch and
// reporting it, looping forever instead of settling into "logged out".
const AUTH_ENDPOINTS_EXEMPT_FROM_UNAUTHORIZED_HANDLER = ['/auth/session', '/auth/login']

async function handleResponse<T>(response: Response, path: string): Promise<T> {
  if (!response.ok) {
    if (response.status === 401 && !AUTH_ENDPOINTS_EXEMPT_FROM_UNAUTHORIZED_HANDLER.includes(path)) {
      unauthorizedHandler?.()
    }
    const problem = await parseProblem(response)
    const message = problem?.detail ?? `Request to ${path} failed with status ${response.status}`
    throw new ApiError(message, response.status, problem)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

// The backend issues a double-submit CSRF cookie (XSRF-TOKEN); mutating
// requests must echo its value back in this header or the session-cookie
// auth's CSRF filter rejects them with 403.
function csrfHeaders(): HeadersInit {
  const token = readCookie('XSRF-TOKEN')
  return token ? { 'X-XSRF-TOKEN': token } : {}
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' })
  return handleResponse<T>(response, path)
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify(body),
  })
  return handleResponse<T>(response, path)
}

export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify(body),
  })
  return handleResponse<T>(response, path)
}

export async function apiDelete(path: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: { ...csrfHeaders() },
  })
  await handleResponse<void>(response, path)
}
