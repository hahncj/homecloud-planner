import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { Project, ProjectInput } from './roadmapTypes'

export function fetchProjects(): Promise<Project[]> {
  return apiGet<Project[]>('/projects')
}

export function fetchProject(projectId: string): Promise<Project> {
  return apiGet<Project>(`/projects/${projectId}`)
}

export function useProjectsQuery() {
  return useQuery({ queryKey: ['projects'], queryFn: fetchProjects })
}

export function useProjectQuery(projectId: string | undefined) {
  return useQuery({
    queryKey: ['projects', projectId],
    queryFn: () => fetchProject(projectId as string),
    enabled: Boolean(projectId),
  })
}

export function useCreateProjectMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: ProjectInput) => apiPost<Project>('/projects', input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useUpdateProjectMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ projectId, input }: { projectId: string; input: ProjectInput }) =>
      apiPut<Project>(`/projects/${projectId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useDeleteProjectMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (projectId: string) => apiDelete(`/projects/${projectId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}
