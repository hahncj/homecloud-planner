import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { ManagedService, ManagedServiceFilters, ManagedServiceInput } from './serviceCatalogTypes'

function buildQueryString(filters: ManagedServiceFilters): string {
  const params = new URLSearchParams()
  if (filters.status) params.set('status', filters.status)
  if (filters.runtimeType) params.set('runtimeType', filters.runtimeType)
  if (filters.sensitivity) params.set('sensitivity', filters.sensitivity)
  if (filters.externallyExposed !== undefined) params.set('externallyExposed', String(filters.externallyExposed))
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchServices(projectId: string, filters: ManagedServiceFilters = {}): Promise<ManagedService[]> {
  return apiGet<ManagedService[]>(`/projects/${projectId}/services${buildQueryString(filters)}`)
}

export function fetchService(serviceId: string): Promise<ManagedService> {
  return apiGet<ManagedService>(`/services/${serviceId}`)
}

export function useServicesQuery(projectId: string | undefined, filters: ManagedServiceFilters = {}) {
  return useQuery({
    queryKey: ['projects', projectId, 'services', filters],
    queryFn: () => fetchServices(projectId as string, filters),
    enabled: Boolean(projectId),
  })
}

export function useServiceQuery(serviceId: string | undefined) {
  return useQuery({
    queryKey: ['services', serviceId],
    queryFn: () => fetchService(serviceId as string),
    enabled: Boolean(serviceId),
  })
}

export function useCreateServiceMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: ManagedServiceInput) => apiPost<ManagedService>(`/projects/${projectId}/services`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['services'] })
    },
  })
}

export function useUpdateServiceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ serviceId, input }: { serviceId: string; input: ManagedServiceInput }) =>
      apiPut<ManagedService>(`/services/${serviceId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['services'] })
    },
  })
}

export function useDeleteServiceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (serviceId: string) => apiDelete(`/services/${serviceId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['services'] })
    },
  })
}

export function useAddServiceDependencyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ serviceId, dependsOnServiceId }: { serviceId: string; dependsOnServiceId: string }) =>
      apiPost(`/services/${serviceId}/dependencies`, { dependsOnServiceId }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['services'] })
    },
  })
}

export function useRemoveServiceDependencyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ serviceId, dependsOnServiceId }: { serviceId: string; dependsOnServiceId: string }) =>
      apiDelete(`/services/${serviceId}/dependencies/${dependsOnServiceId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      void queryClient.invalidateQueries({ queryKey: ['services'] })
    },
  })
}
