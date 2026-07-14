export type LifecycleStatus = 'PLANNED' | 'ACTIVE' | 'SPARE' | 'MAINTENANCE' | 'RETIRED' | 'DISPOSED'

export const LIFECYCLE_STATUSES: LifecycleStatus[] = [
  'PLANNED',
  'ACTIVE',
  'SPARE',
  'MAINTENANCE',
  'RETIRED',
  'DISPOSED',
]

export interface Device {
  id: string
  projectId: string
  name: string
  manufacturer: string | null
  model: string | null
  serialNumber: string | null
  role: string | null
  location: string | null
  hostname: string | null
  ipAddress: string | null
  macAddress: string | null
  vlan: number | null
  operatingSystem: string | null
  firmwareVersion: string | null
  purchaseDate: string | null
  warrantyExpiration: string | null
  lifecycleStatus: LifecycleStatus
  replacementTarget: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface DeviceInput {
  name: string
  manufacturer: string | null
  model: string | null
  serialNumber: string | null
  role: string | null
  location: string | null
  hostname: string | null
  ipAddress: string | null
  macAddress: string | null
  vlan: number | null
  operatingSystem: string | null
  firmwareVersion: string | null
  purchaseDate: string | null
  warrantyExpiration: string | null
  lifecycleStatus: LifecycleStatus
  replacementTarget: string | null
  notes: string | null
}

export interface DeviceFilters {
  lifecycleStatus?: LifecycleStatus
  role?: string
  location?: string
}
