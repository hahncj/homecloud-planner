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
import type { Device, DeviceInput } from '../../api/deviceTypes'
import { LIFECYCLE_STATUSES } from '../../api/deviceTypes'
import { deviceFormSchema } from './schemas'
import type { DeviceFormInput, DeviceFormOutput } from './schemas'

interface DeviceFormDialogProps {
  open: boolean
  device?: Device
  onClose: () => void
  onSubmit: (input: DeviceInput) => Promise<unknown>
  onDelete?: () => void
  errorMessage?: string
}

function toDefaultValues(device?: Device): DeviceFormInput {
  return {
    name: device?.name ?? '',
    manufacturer: device?.manufacturer ?? '',
    model: device?.model ?? '',
    serialNumber: device?.serialNumber ?? '',
    role: device?.role ?? '',
    location: device?.location ?? '',
    hostname: device?.hostname ?? '',
    ipAddress: device?.ipAddress ?? '',
    macAddress: device?.macAddress ?? '',
    vlan: device?.vlan ? String(device.vlan) : '',
    operatingSystem: device?.operatingSystem ?? '',
    firmwareVersion: device?.firmwareVersion ?? '',
    purchaseDate: device?.purchaseDate ?? '',
    warrantyExpiration: device?.warrantyExpiration ?? '',
    lifecycleStatus: device?.lifecycleStatus ?? 'PLANNED',
    replacementTarget: device?.replacementTarget ?? '',
    notes: device?.notes ?? '',
  }
}

export function DeviceFormDialog({ open, device, onClose, onSubmit, onDelete, errorMessage }: DeviceFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<DeviceFormInput, unknown, DeviceFormOutput>({
    resolver: zodResolver(deviceFormSchema),
    values: toDefaultValues(device),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit(values)
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{device ? 'Edit device' : 'New device'}</DialogTitle>
      <form onSubmit={submit} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
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
                name="lifecycleStatus"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Lifecycle status" fullWidth>
                    {LIFECYCLE_STATUSES.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="manufacturer"
                control={control}
                render={({ field }) => <TextField {...field} label="Manufacturer" fullWidth />}
              />
              <Controller
                name="model"
                control={control}
                render={({ field }) => <TextField {...field} label="Model" fullWidth />}
              />
              <Controller
                name="serialNumber"
                control={control}
                render={({ field }) => <TextField {...field} label="Serial number" fullWidth />}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="role"
                control={control}
                render={({ field }) => <TextField {...field} label="Role" fullWidth />}
              />
              <Controller
                name="location"
                control={control}
                render={({ field }) => <TextField {...field} label="Location" fullWidth />}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="hostname"
                control={control}
                render={({ field }) => <TextField {...field} label="Hostname" fullWidth />}
              />
              <Controller
                name="ipAddress"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="IP address"
                    fullWidth
                    error={Boolean(errors.ipAddress)}
                    helperText={errors.ipAddress?.message}
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="macAddress"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="MAC address"
                    fullWidth
                    error={Boolean(errors.macAddress)}
                    helperText={errors.macAddress?.message}
                  />
                )}
              />
              <Controller
                name="vlan"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="VLAN"
                    type="number"
                    fullWidth
                    slotProps={{ htmlInput: { min: 1, max: 4094, step: '1' } }}
                    error={Boolean(errors.vlan)}
                    helperText={errors.vlan?.message}
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="operatingSystem"
                control={control}
                render={({ field }) => <TextField {...field} label="Operating system" fullWidth />}
              />
              <Controller
                name="firmwareVersion"
                control={control}
                render={({ field }) => <TextField {...field} label="Firmware version" fullWidth />}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="purchaseDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Purchase date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
              <Controller
                name="warrantyExpiration"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Warranty expiration"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
              <Controller
                name="replacementTarget"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Replacement target"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
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
          {device && onDelete && (
            <Button color="error" onClick={onDelete} sx={{ mr: 'auto' }}>
              Delete device
            </Button>
          )}
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" loading={isSubmitting}>
            Save
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
