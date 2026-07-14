import { zodResolver } from '@hookform/resolvers/zod'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import { Controller, useForm } from 'react-hook-form'
import type { Phase, PhaseInput } from '../../api/roadmapTypes'
import { phaseFormSchema } from './schemas'
import type { PhaseFormInput, PhaseFormOutput } from './schemas'

interface PhaseFormDialogProps {
  open: boolean
  phase?: Phase
  onClose: () => void
  onSubmit: (input: PhaseInput) => Promise<unknown>
  errorMessage?: string
}

function toDefaultValues(phase?: Phase): PhaseFormInput {
  return {
    name: phase?.name ?? '',
    description: phase?.description ?? '',
  }
}

export function PhaseFormDialog({ open, phase, onClose, onSubmit, errorMessage }: PhaseFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PhaseFormInput, unknown, PhaseFormOutput>({
    resolver: zodResolver(phaseFormSchema),
    values: toDefaultValues(phase),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit({ name: values.name, description: values.description })
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{phase ? 'Edit phase' : 'New phase'}</DialogTitle>
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
