export type ManagedServiceStatus =
  | 'PLANNED'
  | 'INSTALLING'
  | 'CONFIGURING'
  | 'VALIDATING'
  | 'OPERATIONAL'
  | 'DEGRADED'
  | 'DISABLED'
  | 'RETIRED'

export const MANAGED_SERVICE_STATUSES: ManagedServiceStatus[] = [
  'PLANNED',
  'INSTALLING',
  'CONFIGURING',
  'VALIDATING',
  'OPERATIONAL',
  'DEGRADED',
  'DISABLED',
  'RETIRED',
]

export type RuntimeType = 'DOCKER' | 'KUBERNETES' | 'VIRTUAL_MACHINE' | 'BARE_METAL' | 'MANAGED_CLOUD' | 'NAS_NATIVE' | 'OTHER'

export const RUNTIME_TYPES: RuntimeType[] = [
  'DOCKER',
  'KUBERNETES',
  'VIRTUAL_MACHINE',
  'BARE_METAL',
  'MANAGED_CLOUD',
  'NAS_NATIVE',
  'OTHER',
]

export type Sensitivity = 'PUBLIC' | 'INTERNAL' | 'CONFIDENTIAL' | 'HIGHLY_SENSITIVE'

export const SENSITIVITIES: Sensitivity[] = ['PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'HIGHLY_SENSITIVE']

export interface ManagedService {
  id: string
  projectId: string
  hostDeviceId: string | null
  name: string
  purpose: string | null
  description: string | null
  status: ManagedServiceStatus
  runtimeType: RuntimeType
  storageLocation: string | null
  sensitivity: Sensitivity
  externallyExposed: boolean
  authenticationMethod: string | null
  backupPolicy: string | null
  documentationUrl: string | null
  repositoryUrl: string | null
  notes: string | null
  dependsOnServiceIds: string[]
  createdAt: string
  updatedAt: string
}

export interface ManagedServiceInput {
  hostDeviceId: string | null
  name: string
  purpose: string | null
  description: string | null
  status: ManagedServiceStatus
  runtimeType: RuntimeType
  storageLocation: string | null
  sensitivity: Sensitivity
  externallyExposed: boolean
  authenticationMethod: string | null
  backupPolicy: string | null
  documentationUrl: string | null
  repositoryUrl: string | null
  notes: string | null
}

export interface ManagedServiceFilters {
  status?: ManagedServiceStatus
  runtimeType?: RuntimeType
  sensitivity?: Sensitivity
  externallyExposed?: boolean
}
