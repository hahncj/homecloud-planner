import { useQuery } from '@tanstack/react-query'
import { apiGet } from './client'

export interface HealthResponse {
  status: string
  timestamp: string
}

export function fetchHealth(): Promise<HealthResponse> {
  return apiGet<HealthResponse>('/health')
}

export function useHealthQuery() {
  return useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth,
  })
}
