import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { BackupCoverageWarningCounts } from '../../api/dashboardTypes'

interface BackupWarningsSummaryProps {
  warnings: BackupCoverageWarningCounts
}

export function BackupWarningsSummary({ warnings }: BackupWarningsSummaryProps) {
  const items = [
    { label: 'Missing local backup', count: warnings.missingLocalBackupCount },
    { label: 'Missing off-site backup', count: warnings.missingOffsiteBackupCount },
    { label: 'Missing encryption (sensitive off-site)', count: warnings.missingEncryptionForSensitiveOffsiteCount },
    { label: 'Verification overdue', count: warnings.verificationOverdueCount },
  ]

  const hasAnyWarning = items.some((item) => item.count > 0)

  if (!hasAnyWarning) {
    return (
      <Typography variant="body2" color="text.secondary">
        No backup coverage warnings.
      </Typography>
    )
  }

  return (
    <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
      {items
        .filter((item) => item.count > 0)
        .map((item) => (
          <Chip key={item.label} label={`${item.label}: ${item.count}`} color="error" variant="outlined" size="small" />
        ))}
    </Stack>
  )
}
