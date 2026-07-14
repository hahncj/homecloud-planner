import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { BackupPolicy, BackupPolicyFilters, BackupPolicyInput } from './backupTypes'

function buildQueryString(filters: BackupPolicyFilters): string {
  const params = new URLSearchParams()
  if (filters.coverageState) params.set('coverageState', filters.coverageState)
  if (filters.verificationOverdue !== undefined) params.set('verificationOverdue', String(filters.verificationOverdue))
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchBackupPolicies(projectId: string, filters: BackupPolicyFilters = {}): Promise<BackupPolicy[]> {
  return apiGet<BackupPolicy[]>(`/projects/${projectId}/backup-policies${buildQueryString(filters)}`)
}

export function useBackupPoliciesQuery(projectId: string | undefined, filters: BackupPolicyFilters = {}) {
  return useQuery({
    queryKey: ['projects', projectId, 'backup-policies', filters],
    queryFn: () => fetchBackupPolicies(projectId as string, filters),
    enabled: Boolean(projectId),
  })
}

export function useCreateBackupPolicyMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: BackupPolicyInput) => apiPost<BackupPolicy>(`/projects/${projectId}/backup-policies`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useUpdateBackupPolicyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ backupPolicyId, input }: { backupPolicyId: string; input: BackupPolicyInput }) =>
      apiPut<BackupPolicy>(`/backup-policies/${backupPolicyId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useDeleteBackupPolicyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (backupPolicyId: string) => apiDelete(`/backup-policies/${backupPolicyId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}
