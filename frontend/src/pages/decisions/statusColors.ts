import type { ChipProps } from '@mui/material/Chip'
import type { DecisionStatus } from '../../api/decisionTypes'

export function decisionStatusColor(status: DecisionStatus): ChipProps['color'] {
  switch (status) {
    case 'ACCEPTED':
      return 'success'
    case 'PROPOSED':
      return 'info'
    case 'DEPRECATED':
    case 'SUPERSEDED':
      return 'warning'
    case 'REJECTED':
      return 'error'
    default:
      return 'default'
  }
}
