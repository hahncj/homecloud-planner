import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { Phase, PhaseInput } from './roadmapTypes'

export function fetchPhases(projectId: string): Promise<Phase[]> {
  return apiGet<Phase[]>(`/projects/${projectId}/phases`)
}

export function usePhasesQuery(projectId: string | undefined) {
  return useQuery({
    queryKey: ['projects', projectId, 'phases'],
    queryFn: () => fetchPhases(projectId as string),
    enabled: Boolean(projectId),
  })
}

export function useCreatePhaseMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: PhaseInput) => apiPost<Phase>(`/projects/${projectId}/phases`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useUpdatePhaseMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ phaseId, input }: { phaseId: string; input: PhaseInput }) =>
      apiPut<Phase>(`/projects/${projectId}/phases/${phaseId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useDeletePhaseMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (phaseId: string) => apiDelete(`/projects/${projectId}/phases/${phaseId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useReorderPhasesMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (orderedPhaseIds: string[]) =>
      apiPut<Phase[]>(`/projects/${projectId}/phases/reorder`, { orderedPhaseIds }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}
