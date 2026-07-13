import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import type { Phase } from '../../api/roadmapTypes'
import type { PurchaseItemFilters } from '../../api/shoppingTypes'
import { PURCHASE_STATUSES } from '../../api/shoppingTypes'

interface PurchaseFiltersBarProps {
  phases: Phase[]
  categories: string[]
  filters: PurchaseItemFilters
  onChange: (filters: PurchaseItemFilters) => void
}

export function PurchaseFiltersBar({ phases, categories, filters, onChange }: PurchaseFiltersBarProps) {
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
        {PURCHASE_STATUSES.map((status) => (
          <MenuItem key={status} value={status}>
            {status.replaceAll('_', ' ')}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        size="small"
        label="Category"
        value={filters.category ?? ''}
        onChange={(event) => onChange({ ...filters, category: event.target.value || undefined })}
        sx={{ minWidth: 160 }}
      >
        <MenuItem value="">All categories</MenuItem>
        {categories.map((category) => (
          <MenuItem key={category} value={category}>
            {category}
          </MenuItem>
        ))}
      </TextField>

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
    </Stack>
  )
}
