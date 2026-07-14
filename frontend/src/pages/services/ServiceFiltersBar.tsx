import FormControlLabel from '@mui/material/FormControlLabel'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import TextField from '@mui/material/TextField'
import type { ManagedServiceFilters } from '../../api/serviceCatalogTypes'
import { MANAGED_SERVICE_STATUSES, RUNTIME_TYPES, SENSITIVITIES } from '../../api/serviceCatalogTypes'

interface ServiceFiltersBarProps {
  filters: ManagedServiceFilters
  onChange: (filters: ManagedServiceFilters) => void
}

export function ServiceFiltersBar({ filters, onChange }: ServiceFiltersBarProps) {
  return (
    <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', alignItems: 'center' }}>
      <TextField
        select
        size="small"
        label="Status"
        value={filters.status ?? ''}
        onChange={(event) => onChange({ ...filters, status: (event.target.value || undefined) as never })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All statuses</MenuItem>
        {MANAGED_SERVICE_STATUSES.map((status) => (
          <MenuItem key={status} value={status}>
            {status}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Runtime type"
        value={filters.runtimeType ?? ''}
        onChange={(event) => onChange({ ...filters, runtimeType: (event.target.value || undefined) as never })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All runtimes</MenuItem>
        {RUNTIME_TYPES.map((type) => (
          <MenuItem key={type} value={type}>
            {type.replaceAll('_', ' ')}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Sensitivity"
        value={filters.sensitivity ?? ''}
        onChange={(event) => onChange({ ...filters, sensitivity: (event.target.value || undefined) as never })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All sensitivities</MenuItem>
        {SENSITIVITIES.map((sensitivity) => (
          <MenuItem key={sensitivity} value={sensitivity}>
            {sensitivity.replaceAll('_', ' ')}
          </MenuItem>
        ))}
      </TextField>

      <FormControlLabel
        control={
          <Switch
            checked={filters.externallyExposed === true}
            onChange={(event) =>
              onChange({ ...filters, externallyExposed: event.target.checked ? true : undefined })
            }
          />
        }
        label="Externally exposed only"
      />
    </Stack>
  )
}
