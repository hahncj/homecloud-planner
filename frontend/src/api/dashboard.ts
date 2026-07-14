import { useQuery } from '@tanstack/react-query'
import { apiGet } from './client'
import type { DashboardSummary } from './dashboardTypes'

export function fetchDashboard(projectId: string): Promise<DashboardSummary> {
  return apiGet<DashboardSummary>(`/projects/${projectId}/dashboard`)
}

export function useDashboardQuery(projectId: string | undefined) {
  return useQuery({
    queryKey: ['projects', projectId, 'dashboard'],
    queryFn: () => fetchDashboard(projectId as string),
    enabled: Boolean(projectId),
  })
}
