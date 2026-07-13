import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { Task, TaskFilters, TaskInput } from './roadmapTypes'

function buildQueryString(filters: TaskFilters): string {
  const params = new URLSearchParams()
  if (filters.phaseId) params.set('phaseId', filters.phaseId)
  if (filters.status) params.set('status', filters.status)
  if (filters.priority) params.set('priority', filters.priority)
  if (filters.blocked !== undefined) params.set('blocked', String(filters.blocked))
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchTasks(projectId: string, filters: TaskFilters = {}): Promise<Task[]> {
  return apiGet<Task[]>(`/projects/${projectId}/tasks${buildQueryString(filters)}`)
}

export function useTasksQuery(projectId: string | undefined, filters: TaskFilters = {}) {
  return useQuery({
    queryKey: ['projects', projectId, 'tasks', filters],
    queryFn: () => fetchTasks(projectId as string, filters),
    enabled: Boolean(projectId),
  })
}

export function useCreateTaskMutation(projectId: string, phaseId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: TaskInput) =>
      apiPost<Task>(`/projects/${projectId}/phases/${phaseId}/tasks`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useUpdateTaskMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ taskId, input }: { taskId: string; input: TaskInput }) =>
      apiPut<Task>(`/tasks/${taskId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useDeleteTaskMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (taskId: string) => apiDelete(`/tasks/${taskId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useAddDependencyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ taskId, dependsOnTaskId }: { taskId: string; dependsOnTaskId: string }) =>
      apiPost(`/tasks/${taskId}/dependencies`, { dependsOnTaskId }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useRemoveDependencyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ taskId, dependsOnTaskId }: { taskId: string; dependsOnTaskId: string }) =>
      apiDelete(`/tasks/${taskId}/dependencies/${dependsOnTaskId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}
