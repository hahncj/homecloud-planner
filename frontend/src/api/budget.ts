import { useQuery } from '@tanstack/react-query'
import { apiGet } from './client'
import type { BudgetSummary } from './shoppingTypes'

export function fetchBudgetSummary(projectId: string): Promise<BudgetSummary> {
  return apiGet<BudgetSummary>(`/projects/${projectId}/budget`)
}

export function useBudgetQuery(projectId: string | undefined) {
  return useQuery({
    queryKey: ['projects', projectId, 'budget'],
    queryFn: () => fetchBudgetSummary(projectId as string),
    enabled: Boolean(projectId),
  })
}
