import { apiGet, apiPost } from './client'
import type { LoginRequest, SessionResponse } from './authTypes'

export function fetchSession(): Promise<SessionResponse> {
  return apiGet<SessionResponse>('/auth/session')
}

export function login(request: LoginRequest): Promise<SessionResponse> {
  return apiPost<SessionResponse>('/auth/login', request)
}

export function logout(): Promise<void> {
  return apiPost<void>('/auth/logout', undefined)
}
