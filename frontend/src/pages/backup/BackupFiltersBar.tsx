import FormControlLabel from '@mui/material/FormControlLabel'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import TextField from '@mui/material/TextField'
import type { BackupPolicyFilters } from '../../api/backupTypes'
import { BACKUP_COVERAGE_STATES } from '../../api/backupTypes'

interface BackupFiltersBarProps {
  filters: BackupPolicyFilters
  onChange: (filters: BackupPolicyFilters) => void
}

export function BackupFiltersBar({ filters, onChange }: BackupFiltersBarProps) {
  return (
    <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', alignItems: 'center' }}>
      <TextField
        select
        size="small"
        label="Coverage"
        value={filters.coverageState ?? ''}
        onChange={(event) => onChange({ ...filters, coverageState: (event.target.value || undefined) as never })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All coverage levels</MenuItem>
        {BACKUP_COVERAGE_STATES.map((state) => (
          <MenuItem key={state} value={state}>
            {state}
          </MenuItem>
        ))}
      </TextField>

      <FormControlLabel
        control={
          <Switch
            checked={filters.verificationOverdue === true}
            onChange={(event) =>
              onChange({ ...filters, verificationOverdue: event.target.checked ? true : undefined })
            }
          />
        }
        label="Verification overdue only"
      />
    </Stack>
  )
}
