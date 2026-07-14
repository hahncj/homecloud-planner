import FormControlLabel from '@mui/material/FormControlLabel'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import TextField from '@mui/material/TextField'
import type { Phase, TaskFilters } from '../../api/roadmapTypes'
import { TASK_PRIORITIES, TASK_STATUSES } from '../../api/roadmapTypes'

interface TaskFiltersBarProps {
  phases: Phase[]
  filters: TaskFilters
  onChange: (filters: TaskFilters) => void
}

export function TaskFiltersBar({ phases, filters, onChange }: TaskFiltersBarProps) {
  return (
    <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', alignItems: 'center' }}>
      <TextField
        select
        size="small"
        label="Phase"
        value={filters.phaseId ?? ''}
        onChange={(event) => onChange({ ...filters, phaseId: event.target.value || undefined })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All phases</MenuItem>
        {phases.map((phase) => (
          <MenuItem key={phase.id} value={phase.id}>
            {phase.name}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Status"
        value={filters.status ?? ''}
        onChange={(event) => onChange({ ...filters, status: (event.target.value || undefined) as never })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All statuses</MenuItem>
        {TASK_STATUSES.map((status) => (
          <MenuItem key={status} value={status}>
            {status.replaceAll('_', ' ')}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Priority"
        value={filters.priority ?? ''}
        onChange={(event) => onChange({ ...filters, priority: (event.target.value || undefined) as never })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All priorities</MenuItem>
        {TASK_PRIORITIES.map((priority) => (
          <MenuItem key={priority} value={priority}>
            {priority}
          </MenuItem>
        ))}
      </TextField>

      <FormControlLabel
        control={
          <Switch
            checked={filters.blocked === true}
            onChange={(event) => onChange({ ...filters, blocked: event.target.checked ? true : undefined })}
          />
        }
        label="Blocked only"
      />
    </Stack>
  )
}
