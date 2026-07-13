import AddIcon from '@mui/icons-material/Add'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined'
import Alert from '@mui/material/Alert'
import Chip from '@mui/material/Chip'
import IconButton from '@mui/material/IconButton'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useState } from 'react'
import type { ManagedService } from '../../api/serviceCatalogTypes'

interface ServiceDependencySectionProps {
  service: ManagedService
  allServices: ManagedService[]
  onAdd: (dependsOnServiceId: string) => Promise<unknown>
  onRemove: (dependsOnServiceId: string) => Promise<unknown>
}

export function ServiceDependencySection({ service, allServices, onAdd, onRemove }: ServiceDependencySectionProps) {
  const [selected, setSelected] = useState('')
  const [error, setError] = useState<string | undefined>(undefined)

  const dependencyServices = service.dependsOnServiceIds
    .map((id) => allServices.find((candidate) => candidate.id === id))
    .filter((candidate): candidate is ManagedService => candidate !== undefined)

  const availableServices = allServices.filter(
    (candidate) => candidate.id !== service.id && !service.dependsOnServiceIds.includes(candidate.id),
  )

  async function handleAdd() {
    if (!selected) return
    setError(undefined)
    try {
      await onAdd(selected)
      setSelected('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to add dependency')
    }
  }

  return (
    <Stack spacing={1}>
      {error && <Alert severity="error">{error}</Alert>}

      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
        {dependencyServices.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No dependencies.
          </Typography>
        )}
        {dependencyServices.map((dependency) => (
          <Chip
            key={dependency.id}
            label={dependency.name}
            onDelete={() => onRemove(dependency.id)}
            deleteIcon={<DeleteOutlineIcon />}
          />
        ))}
      </Stack>

      {availableServices.length > 0 && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <TextField
            select
            size="small"
            label="Add dependency"
            value={selected}
            onChange={(event) => setSelected(event.target.value)}
            sx={{ minWidth: 240 }}
          >
            {availableServices.map((candidate) => (
              <MenuItem key={candidate.id} value={candidate.id}>
                {candidate.name}
              </MenuItem>
            ))}
          </TextField>
          <IconButton aria-label="Add dependency" color="primary" disabled={!selected} onClick={handleAdd}>
            <AddIcon />
          </IconButton>
        </Stack>
      )}
    </Stack>
  )
}
