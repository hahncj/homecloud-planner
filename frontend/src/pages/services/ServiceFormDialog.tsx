import { zodResolver } from '@hookform/resolvers/zod'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControlLabel from '@mui/material/FormControlLabel'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import TextField from '@mui/material/TextField'
import { Controller, useForm } from 'react-hook-form'
import type { Device } from '../../api/deviceTypes'
import type { ManagedService, ManagedServiceInput } from '../../api/serviceCatalogTypes'
import { MANAGED_SERVICE_STATUSES, RUNTIME_TYPES, SENSITIVITIES } from '../../api/serviceCatalogTypes'
import { serviceFormSchema } from './schemas'
import type { ServiceFormInput, ServiceFormOutput } from './schemas'

interface ServiceFormDialogProps {
  open: boolean
  service?: ManagedService
  devices: Device[]
  onClose: () => void
  onSubmit: (input: ManagedServiceInput) => Promise<unknown>
  errorMessage?: string
}

function toDefaultValues(service?: ManagedService): ServiceFormInput {
  return {
    hostDeviceId: service?.hostDeviceId ?? '',
    name: service?.name ?? '',
    purpose: service?.purpose ?? '',
    description: service?.description ?? '',
    status: service?.status ?? 'PLANNED',
    runtimeType: service?.runtimeType ?? 'DOCKER',
    storageLocation: service?.storageLocation ?? '',
    sensitivity: service?.sensitivity ?? 'INTERNAL',
    externallyExposed: service?.externallyExposed ?? false,
    authenticationMethod: service?.authenticationMethod ?? '',
    backupPolicy: service?.backupPolicy ?? '',
    documentationUrl: service?.documentationUrl ?? '',
    repositoryUrl: service?.repositoryUrl ?? '',
    notes: service?.notes ?? '',
  }
}

export function ServiceFormDialog({ open, service, devices, onClose, onSubmit, errorMessage }: ServiceFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ServiceFormInput, unknown, ServiceFormOutput>({
    resolver: zodResolver(serviceFormSchema),
    values: toDefaultValues(service),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit({
      hostDeviceId: values.hostDeviceId === '' ? null : values.hostDeviceId,
      name: values.name,
      purpose: values.purpose,
      description: values.description,
      status: values.status,
      runtimeType: values.runtimeType,
      storageLocation: values.storageLocation,
      sensitivity: values.sensitivity,
      externallyExposed: values.externallyExposed,
      authenticationMethod: values.authenticationMethod,
      backupPolicy: values.backupPolicy,
      documentationUrl: values.documentationUrl,
      repositoryUrl: values.repositoryUrl,
      notes: values.notes,
    })
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{service ? 'Edit service' : 'New service'}</DialogTitle>
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
              name="purpose"
              control={control}
              render={({ field }) => <TextField {...field} label="Purpose" fullWidth />}
            />

            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Description" fullWidth multiline minRows={2} />
              )}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="status"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Status" fullWidth>
                    {MANAGED_SERVICE_STATUSES.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <Controller
                name="runtimeType"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Runtime type" fullWidth>
                    {RUNTIME_TYPES.map((type) => (
                      <MenuItem key={type} value={type}>
                        {type.replaceAll('_', ' ')}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="hostDeviceId"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Host device" fullWidth>
                    <MenuItem value="">None</MenuItem>
                    {devices.map((device) => (
                      <MenuItem key={device.id} value={device.id}>
                        {device.name}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <Controller
                name="storageLocation"
                control={control}
                render={({ field }) => <TextField {...field} label="Storage location" fullWidth />}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
              <Controller
                name="sensitivity"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Sensitivity" fullWidth>
                    {SENSITIVITIES.map((sensitivity) => (
                      <MenuItem key={sensitivity} value={sensitivity}>
                        {sensitivity.replaceAll('_', ' ')}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <Controller
                name="externallyExposed"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                    label="Externally exposed"
                  />
                )}
              />
            </Stack>

            <Controller
              name="authenticationMethod"
              control={control}
              render={({ field }) => <TextField {...field} label="Authentication method" fullWidth />}
            />

            <Controller
              name="backupPolicy"
              control={control}
              render={({ field }) => <TextField {...field} label="Backup policy" fullWidth />}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="documentationUrl"
                control={control}
                render={({ field }) => <TextField {...field} label="Documentation URL" fullWidth />}
              />
              <Controller
                name="repositoryUrl"
                control={control}
                render={({ field }) => <TextField {...field} label="Repository URL" fullWidth />}
              />
            </Stack>

            <Controller
              name="notes"
              control={control}
              render={({ field }) => <TextField {...field} label="Notes" fullWidth multiline minRows={2} />}
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
