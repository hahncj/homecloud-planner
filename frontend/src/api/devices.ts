import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from './client'
import type { Device, DeviceFilters, DeviceInput } from './deviceTypes'

function buildQueryString(filters: DeviceFilters): string {
  const params = new URLSearchParams()
  if (filters.lifecycleStatus) params.set('lifecycleStatus', filters.lifecycleStatus)
  if (filters.role) params.set('role', filters.role)
  if (filters.location) params.set('location', filters.location)
  const query = params.toString()
  return query ? `?${query}` : ''
}

export function fetchDevices(projectId: string, filters: DeviceFilters = {}): Promise<Device[]> {
  return apiGet<Device[]>(`/projects/${projectId}/devices${buildQueryString(filters)}`)
}

export function useDevicesQuery(projectId: string | undefined, filters: DeviceFilters = {}) {
  return useQuery({
    queryKey: ['projects', projectId, 'devices', filters],
    queryFn: () => fetchDevices(projectId as string, filters),
    enabled: Boolean(projectId),
  })
}

export function useCreateDeviceMutation(projectId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: DeviceInput) => apiPost<Device>(`/projects/${projectId}/devices`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useUpdateDeviceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ deviceId, input }: { deviceId: string; input: DeviceInput }) =>
      apiPut<Device>(`/devices/${deviceId}`, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}

export function useDeleteDeviceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (deviceId: string) => apiDelete(`/devices/${deviceId}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
    },
  })
}
