import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import type { DeviceFilters } from '../../api/deviceTypes'
import { LIFECYCLE_STATUSES } from '../../api/deviceTypes'

interface DeviceFiltersBarProps {
  roles: string[]
  locations: string[]
  filters: DeviceFilters
  onChange: (filters: DeviceFilters) => void
}

export function DeviceFiltersBar({ roles, locations, filters, onChange }: DeviceFiltersBarProps) {
  return (
    <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', alignItems: 'center' }}>
      <TextField
        select
        size="small"
        label="Lifecycle status"
        value={filters.lifecycleStatus ?? ''}
        onChange={(event) => onChange({ ...filters, lifecycleStatus: (event.target.value || undefined) as never })}
        sx={{ minWidth: 180 }}
      >
        <MenuItem value="">All statuses</MenuItem>
        {LIFECYCLE_STATUSES.map((status) => (
          <MenuItem key={status} value={status}>
            {status}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Role"
        value={filters.role ?? ''}
        onChange={(event) => onChange({ ...filters, role: event.target.value || undefined })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All roles</MenuItem>
        {roles.map((role) => (
          <MenuItem key={role} value={role}>
            {role}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Location"
        value={filters.location ?? ''}
        onChange={(event) => onChange({ ...filters, location: event.target.value || undefined })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All locations</MenuItem>
        {locations.map((location) => (
          <MenuItem key={location} value={location}>
            {location}
          </MenuItem>
        ))}
      </TextField>
    </Stack>
  )
}
