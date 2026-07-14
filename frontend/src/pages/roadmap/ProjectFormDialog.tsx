import { zodResolver } from '@hookform/resolvers/zod'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import { Controller, useForm } from 'react-hook-form'
import type { Project, ProjectInput } from '../../api/roadmapTypes'
import { PROJECT_STATUSES } from '../../api/roadmapTypes'
import { projectFormSchema } from './schemas'
import type { ProjectFormInput, ProjectFormOutput } from './schemas'

interface ProjectFormDialogProps {
  open: boolean
  project?: Project
  onClose: () => void
  onSubmit: (input: ProjectInput) => Promise<unknown>
  errorMessage?: string
}

function toDefaultValues(project?: Project): ProjectFormInput {
  return {
    name: project?.name ?? '',
    description: project?.description ?? '',
    status: project?.status ?? 'PLANNING',
    budget: project?.budget ?? '',
    startDate: project?.startDate ?? '',
    targetDate: project?.targetDate ?? '',
  }
}

export function ProjectFormDialog({ open, project, onClose, onSubmit, errorMessage }: ProjectFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProjectFormInput, unknown, ProjectFormOutput>({
    resolver: zodResolver(projectFormSchema),
    values: toDefaultValues(project),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit({
      name: values.name,
      description: values.description,
      status: values.status,
      budget: values.budget,
      startDate: values.startDate,
      targetDate: values.targetDate,
    })
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{project ? 'Edit project' : 'New project'}</DialogTitle>
      <form onSubmit={submit} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            <Controller
              name="name"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Name"
                  required
                  fullWidth
                  autoFocus
                  error={Boolean(errors.name)}
                  helperText={errors.name?.message}
                />
              )}
            />

            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Description" fullWidth multiline minRows={2} />
              )}
            />

            <Controller
              name="status"
              control={control}
              render={({ field }) => (
                <TextField {...field} select label="Status" fullWidth>
                  {PROJECT_STATUSES.map((status) => (
                    <MenuItem key={status} value={status}>
                      {status.replaceAll('_', ' ')}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />

            <Controller
              name="budget"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Budget"
                  type="number"
                  fullWidth
                  slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                  error={Boolean(errors.budget)}
                  helperText={errors.budget?.message}
                />
              )}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="startDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Start date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
              <Controller
                name="targetDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Target date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
            </Stack>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" loading={isSubmitting}>
            Save
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
