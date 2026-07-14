import AddIcon from '@mui/icons-material/Add'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Chip from '@mui/material/Chip'
import Divider from '@mui/material/Divider'
import IconButton from '@mui/material/IconButton'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useState } from 'react'
import type { Task } from '../../api/roadmapTypes'

interface DependencySectionProps {
  task: Task
  allTasks: Task[]
  onAdd: (dependsOnTaskId: string) => Promise<unknown>
  onRemove: (dependsOnTaskId: string) => Promise<unknown>
}

export function DependencySection({ task, allTasks, onAdd, onRemove }: DependencySectionProps) {
  const [selected, setSelected] = useState('')
  const [error, setError] = useState<string | undefined>(undefined)

  const dependencyTasks = task.dependsOnTaskIds
    .map((id) => allTasks.find((candidate) => candidate.id === id))
    .filter((candidate): candidate is Task => candidate !== undefined)

  const availableTasks = allTasks.filter(
    (candidate) => candidate.id !== task.id && !task.dependsOnTaskIds.includes(candidate.id),
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
    <Box>
      <Divider sx={{ my: 1 }} />
      <Typography variant="subtitle2" gutterBottom>
        Depends on
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 1 }}>
          {error}
        </Alert>
      )}

      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', mb: 1 }}>
        {dependencyTasks.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No dependencies.
          </Typography>
        )}
        {dependencyTasks.map((dependency) => (
          <Chip
            key={dependency.id}
            label={dependency.title}
            onDelete={() => onRemove(dependency.id)}
            deleteIcon={<DeleteOutlineIcon />}
          />
        ))}
      </Stack>

      {availableTasks.length > 0 && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <TextField
            select
            size="small"
            label="Add dependency"
            value={selected}
            onChange={(event) => setSelected(event.target.value)}
            sx={{ minWidth: 240 }}
          >
            {availableTasks.map((candidate) => (
              <MenuItem key={candidate.id} value={candidate.id}>
                {candidate.title}
              </MenuItem>
            ))}
          </TextField>
          <IconButton aria-label="Add dependency" color="primary" disabled={!selected} onClick={handleAdd}>
            <AddIcon />
          </IconButton>
        </Stack>
      )}
    </Box>
  )
}
