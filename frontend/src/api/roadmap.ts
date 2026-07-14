import { useQuery } from '@tanstack/react-query'
import { apiGet } from './client'
import type { Roadmap } from './roadmapTypes'

export function fetchRoadmap(projectId: string): Promise<Roadmap> {
  return apiGet<Roadmap>(`/projects/${projectId}/roadmap`)
}

export function useRoadmapQuery(projectId: string | undefined) {
  return useQuery({
    queryKey: ['projects', projectId, 'roadmap'],
    queryFn: () => fetchRoadmap(projectId as string),
    enabled: Boolean(projectId),
  })
}
