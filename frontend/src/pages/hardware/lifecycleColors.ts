import type { ChipProps } from '@mui/material/Chip'
import type { LifecycleStatus } from '../../api/deviceTypes'

export function lifecycleColor(status: LifecycleStatus): ChipProps['color'] {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'PLANNED':
      return 'info'
    case 'SPARE':
      return 'default'
    case 'MAINTENANCE':
      return 'warning'
    case 'RETIRED':
    case 'DISPOSED':
      return 'error'
    default:
      return 'default'
  }
}
