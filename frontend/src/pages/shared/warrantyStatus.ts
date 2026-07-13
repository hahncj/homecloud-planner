import type { ChipProps } from '@mui/material/Chip'

export type WarrantyState = 'none' | 'expired' | 'expiring-soon' | 'active'

const EXPIRING_SOON_DAYS = 30

export function warrantyState(warrantyExpiration: string | null, today: Date = new Date()): WarrantyState {
  if (!warrantyExpiration) return 'none'
  const expiration = new Date(warrantyExpiration)
  const daysRemaining = Math.floor((expiration.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  if (daysRemaining < 0) return 'expired'
  if (daysRemaining <= EXPIRING_SOON_DAYS) return 'expiring-soon'
  return 'active'
}

export function warrantyLabel(state: WarrantyState): string {
  switch (state) {
    case 'expired':
      return 'Warranty expired'
    case 'expiring-soon':
      return 'Warranty expiring soon'
    case 'active':
      return 'Under warranty'
    case 'none':
    default:
      return 'No warranty on file'
  }
}

export function warrantyColor(state: WarrantyState): ChipProps['color'] {
  switch (state) {
    case 'expired':
      return 'error'
    case 'expiring-soon':
      return 'warning'
    case 'active':
      return 'success'
    case 'none':
    default:
      return 'default'
  }
}
