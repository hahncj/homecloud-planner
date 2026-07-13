import type { ChipProps } from '@mui/material/Chip'
import type { TaskPriority, TaskStatus } from '../../api/roadmapTypes'

export function statusColor(status: TaskStatus): ChipProps['color'] {
  switch (status) {
    case 'COMPLETED':
      return 'success'
    case 'IN_PROGRESS':
      return 'info'
    case 'CANCELLED':
      return 'default'
    case 'NOT_STARTED':
    default:
      return 'default'
  }
}

export function priorityColor(priority: TaskPriority): ChipProps['color'] {
  switch (priority) {
    case 'CRITICAL':
      return 'error'
    case 'HIGH':
      return 'warning'
    case 'MEDIUM':
      return 'info'
    case 'LOW':
    default:
      return 'default'
  }
}
