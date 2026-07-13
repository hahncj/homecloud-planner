import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { PurchaseItem, PurchaseItemFilters, PurchaseItemInput } from './shoppingTypes'

function buildQueryString(filters: PurchaseItemFilters): string {
  const params = new URLSearchParams()
  if (filters.status) params.set('status', filters.status)
  if (filters.category) params.set('category', filters.category)
  if (filters.phaseId) params.set('phaseId', filters.phaseId)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchPurchaseItems(projectId: string, filters: PurchaseItemFilters = {}): Promise<PurchaseItem[]> {
  return apiGet<PurchaseItem[]>(`/projects/${projectId}/purchase-items${buildQueryString(filters)}`)
}

export function usePurchaseItemsQuery(projectId: string | undefined, filters: PurchaseItemFilters = {}) {
  return useQuery({
    queryKey: ['projects', projectId, 'purchase-items', filters],
    queryFn: () => fetchPurchaseItems(projectId as string, filters),
    enabled: Boolean(projectId),
  })
}

export function useCreatePurchaseItemMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: PurchaseItemInput) => apiPost<PurchaseItem>(`/projects/${projectId}/purchase-items`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useUpdatePurchaseItemMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ purchaseItemId, input }: { purchaseItemId: string; input: PurchaseItemInput }) =>
      apiPut<PurchaseItem>(`/purchase-items/${purchaseItemId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useDeletePurchaseItemMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (purchaseItemId: string) => apiDelete(`/purchase-items/${purchaseItemId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}
