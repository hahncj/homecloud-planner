import type { ChipProps } from '@mui/material/Chip'
import type { BackupCoverageState } from '../../api/backupTypes'

export function coverageColor(state: BackupCoverageState): ChipProps['color'] {
  switch (state) {
    case 'FULL':
      return 'success'
    case 'PARTIAL':
      return 'warning'
    case 'NONE':
    default:
      return 'error'
  }
}
