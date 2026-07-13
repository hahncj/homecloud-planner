import type { ChipProps } from '@mui/material/Chip'
import type { ManagedServiceStatus, Sensitivity } from '../../api/serviceCatalogTypes'

export function serviceStatusColor(status: ManagedServiceStatus): ChipProps['color'] {
  switch (status) {
    case 'OPERATIONAL':
      return 'success'
    case 'DEGRADED':
      return 'warning'
    case 'DISABLED':
    case 'RETIRED':
      return 'error'
    case 'PLANNED':
    case 'INSTALLING':
    case 'CONFIGURING':
    case 'VALIDATING':
    default:
      return 'info'
  }
}

export function sensitivityColor(sensitivity: Sensitivity): ChipProps['color'] {
  switch (sensitivity) {
    case 'PUBLIC':
      return 'default'
    case 'INTERNAL':
      return 'info'
    case 'CONFIDENTIAL':
      return 'warning'
    case 'HIGHLY_SENSITIVE':
      return 'error'
    default:
      return 'default'
  }
}
