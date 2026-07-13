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

async function handleResponse<T>(response: Response, path: string): Promise<T> {
  if (!response.ok) {
    const problem = await parseProblem(response)
    const message = problem?.detail ?? `Request to ${path} failed with status ${response.status}`
    throw new ApiError(message, response.status, problem)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`)
  return handleResponse<T>(response, path)
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  return handleResponse<T>(response, path)
}

export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  return handleResponse<T>(response, path)
}

export async function apiDelete(path: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, { method: 'DELETE' })
  await handleResponse<void>(response, path)
}
