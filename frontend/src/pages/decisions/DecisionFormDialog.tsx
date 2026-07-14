import { zodResolver } from '@hookform/resolvers/zod'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import { Controller, useForm } from 'react-hook-form'
import type { Device } from '../../api/deviceTypes'
import type { ArchitectureDecision, ArchitectureDecisionInput } from '../../api/decisionTypes'
import { DECISION_STATUSES } from '../../api/decisionTypes'
import type { ManagedService } from '../../api/serviceCatalogTypes'
import { decisionFormSchema } from './schemas'
import type { DecisionFormInput, DecisionFormOutput } from './schemas'

interface DecisionFormDialogProps {
  open: boolean
  decision?: ArchitectureDecision
  devices: Device[]
  services: ManagedService[]
  onClose: () => void
  onSubmit: (input: ArchitectureDecisionInput) => Promise<unknown>
  errorMessage?: string
}

function toDefaultValues(decision?: ArchitectureDecision): DecisionFormInput {
  return {
    title: decision?.title ?? '',
    status: decision?.status ?? 'PROPOSED',
    context: decision?.context ?? '',
    decision: decision?.decision ?? '',
    alternativesConsidered: decision?.alternativesConsidered ?? '',
    consequences: decision?.consequences ?? '',
    decisionDate: decision?.decisionDate ?? '',
    revisitCriteria: decision?.revisitCriteria ?? '',
    relatedDeviceIds: decision?.relatedDevices.map((device) => device.id) ?? [],
    relatedServiceIds: decision?.relatedServices.map((service) => service.id) ?? [],
  }
}

export function DecisionFormDialog({
  open,
  decision,
  devices,
  services,
  onClose,
  onSubmit,
  errorMessage,
}: DecisionFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<DecisionFormInput, unknown, DecisionFormOutput>({
    resolver: zodResolver(decisionFormSchema),
    values: toDefaultValues(decision),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit(values)
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{decision ? 'Edit decision' : 'New decision'}</DialogTitle>
      <form onSubmit={submit} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="title"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Title"
                    required
                    fullWidth
                    autoFocus
                    error={Boolean(errors.title)}
                    helperText={errors.title?.message}
                  />
                )}
              />
              <Controller
                name="status"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Status" fullWidth>
                    {DECISION_STATUSES.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Stack>

            <Controller
              name="context"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Context" fullWidth multiline minRows={2} />
              )}
            />

            <Controller
              name="decision"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Decision"
                  required
                  fullWidth
                  multiline
                  minRows={2}
                  error={Boolean(errors.decision)}
                  helperText={errors.decision?.message}
                />
              )}
            />

            <Controller
              name="alternativesConsidered"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Alternatives considered" fullWidth multiline minRows={2} />
              )}
            />

            <Controller
              name="consequences"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Consequences" fullWidth multiline minRows={2} />
              )}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="decisionDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Decision date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
            </Stack>

            <Controller
              name="revisitCriteria"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Revisit criteria" fullWidth multiline minRows={2} />
              )}
            />

            <Controller
              name="relatedDeviceIds"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  select
                  label="Related devices"
                  fullWidth
                  slotProps={{
                    select: {
                      multiple: true,
                      renderValue: (selected) => (
                        <Stack direction="row" spacing={0.5} sx={{ flexWrap: 'wrap' }}>
                          {(selected as string[]).map((id) => (
                            <Chip key={id} size="small" label={devices.find((d) => d.id === id)?.name ?? id} />
                          ))}
                        </Stack>
                      ),
                    },
                  }}
                >
                  {devices.map((device) => (
                    <MenuItem key={device.id} value={device.id}>
                      {device.name}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />

            <Controller
              name="relatedServiceIds"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  select
                  label="Related services"
                  fullWidth
                  slotProps={{
                    select: {
                      multiple: true,
                      renderValue: (selected) => (
                        <Stack direction="row" spacing={0.5} sx={{ flexWrap: 'wrap' }}>
                          {(selected as string[]).map((id) => (
                            <Chip key={id} size="small" label={services.find((s) => s.id === id)?.name ?? id} />
                          ))}
                        </Stack>
                      ),
                    },
                  }}
                >
                  {services.map((service) => (
                    <MenuItem key={service.id} value={service.id}>
                      {service.name}
                    </MenuItem>
                  ))}
                </TextField>
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
