import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { ArchitectureDecision, ArchitectureDecisionFilters, ArchitectureDecisionInput } from './decisionTypes'

function buildQueryString(filters: ArchitectureDecisionFilters): string {
  const params = new URLSearchParams()
  if (filters.status) params.set('status', filters.status)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchDecisions(
  projectId: string,
  filters: ArchitectureDecisionFilters = {},
): Promise<ArchitectureDecision[]> {
  return apiGet<ArchitectureDecision[]>(`/projects/${projectId}/decisions${buildQueryString(filters)}`)
}

export function fetchDecision(decisionId: string): Promise<ArchitectureDecision> {
  return apiGet<ArchitectureDecision>(`/decisions/${decisionId}`)
}

export function useDecisionsQuery(projectId: string | undefined, filters: ArchitectureDecisionFilters = {}) {
  return useQuery({
    queryKey: ['projects', projectId, 'decisions', filters],
    queryFn: () => fetchDecisions(projectId as string, filters),
    enabled: Boolean(projectId),
  })
}

export function useDecisionQuery(decisionId: string | undefined) {
  return useQuery({
    queryKey: ['decisions', decisionId],
    queryFn: () => fetchDecision(decisionId as string),
    enabled: Boolean(decisionId),
  })
}

export function useCreateDecisionMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: ArchitectureDecisionInput) =>
      apiPost<ArchitectureDecision>(`/projects/${projectId}/decisions`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['decisions'] })
    },
  })
}

export function useUpdateDecisionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ decisionId, input }: { decisionId: string; input: ArchitectureDecisionInput }) =>
      apiPut<ArchitectureDecision>(`/decisions/${decisionId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['decisions'] })
    },
  })
}

export function useDeleteDecisionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (decisionId: string) => apiDelete(`/decisions/${decisionId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['decisions'] })
    },
  })
}
