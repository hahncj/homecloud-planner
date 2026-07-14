import { createContext, useContext, useEffect } from 'react'
import type { ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchSession, login as loginRequest, logout as logoutRequest } from '../api/auth'
import type { LoginRequest, SessionResponse } from '../api/authTypes'
import { ApiError, setUnauthorizedHandler } from '../api/client'

const SESSION_QUERY_KEY = ['auth', 'session']

interface AuthContextValue {
  user: SessionResponse | undefined
  isPending: boolean
  isAuthenticated: boolean
  login: (credentials: LoginRequest) => Promise<void>
  logout: () => Promise<void>
  isLoggingIn: boolean
  loginError: string | undefined
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

/**
 * Wraps the single-admin session-cookie auth described in ADR-0008. The
 * session check is a normal query (so it shares TanStack Query's cache/
 * loading state) rather than ad hoc component state, and a 401 from *any*
 * request — not just this one — clears it via setUnauthorizedHandler, so a
 * session that expires mid-use still bounces the user back to /login.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const queryClient = useQueryClient()

  const sessionQuery = useQuery({
    queryKey: SESSION_QUERY_KEY,
    queryFn: fetchSession,
    retry: false,
  })

  useEffect(() => {
    setUnauthorizedHandler(() => {
      queryClient.removeQueries({ queryKey: SESSION_QUERY_KEY })
    })
    return () => setUnauthorizedHandler(undefined)
  }, [queryClient])

  const loginMutation = useMutation({
    mutationFn: loginRequest,
    onSuccess: (session) => {
      queryClient.setQueryData(SESSION_QUERY_KEY, session)
    },
  })

  const logoutMutation = useMutation({
    mutationFn: logoutRequest,
    onSettled: () => {
      // Clears every cached query, not just the session: logging out should
      // never leave the next login able to see a previous session's data
      // flash on screen before its own queries resolve.
      queryClient.clear()
    },
  })

  const value: AuthContextValue = {
    user: sessionQuery.data,
    isPending: sessionQuery.isPending,
    isAuthenticated: Boolean(sessionQuery.data),
    login: async (credentials) => {
      await loginMutation.mutateAsync(credentials)
    },
    logout: async () => {
      await logoutMutation.mutateAsync()
    },
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error instanceof ApiError ? loginMutation.error.message : undefined,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
