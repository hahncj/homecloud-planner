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
import type { BackupPolicy, BackupPolicyInput } from '../../api/backupTypes'
import { BACKUP_FREQUENCIES } from '../../api/backupTypes'
import { backupPolicyFormSchema } from './schemas'
import type { BackupPolicyFormInput, BackupPolicyFormOutput } from './schemas'

interface BackupPolicyFormDialogProps {
  open: boolean
  policy?: BackupPolicy
  onClose: () => void
  onSubmit: (input: BackupPolicyInput) => Promise<unknown>
  onDelete?: () => void
  errorMessage?: string
}

function toDefaultValues(policy?: BackupPolicy): BackupPolicyFormInput {
  return {
    name: policy?.name ?? '',
    dataCategory: policy?.dataCategory ?? '',
    primaryLocation: policy?.primaryLocation ?? '',
    localBackupLocation: policy?.localBackupLocation ?? '',
    offsiteBackupLocation: policy?.offsiteBackupLocation ?? '',
    encrypted: policy?.encrypted ?? false,
    containsSensitiveData: policy?.containsSensitiveData ?? false,
    frequency: policy?.frequency ?? 'DAILY',
    retention: policy?.retention ?? '',
    recoveryPointObjective: policy?.recoveryPointObjective ?? '',
    recoveryTimeObjective: policy?.recoveryTimeObjective ?? '',
    lastVerifiedDate: policy?.lastVerifiedDate ?? '',
    verificationNotes: policy?.verificationNotes ?? '',
  }
}

export function BackupPolicyFormDialog({
  open,
  policy,
  onClose,
  onSubmit,
  onDelete,
  errorMessage,
}: BackupPolicyFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<BackupPolicyFormInput, unknown, BackupPolicyFormOutput>({
    resolver: zodResolver(backupPolicyFormSchema),
    values: toDefaultValues(policy),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit(values)
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{policy ? 'Edit backup policy' : 'New backup policy'}</DialogTitle>
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
                name="dataCategory"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Data category"
                    required
                    fullWidth
                    error={Boolean(errors.dataCategory)}
                    helperText={errors.dataCategory?.message}
                  />
                )}
              />
            </Stack>

            <Controller
              name="primaryLocation"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Primary location"
                  required
                  fullWidth
                  error={Boolean(errors.primaryLocation)}
                  helperText={errors.primaryLocation?.message}
                />
              )}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="localBackupLocation"
                control={control}
                render={({ field }) => <TextField {...field} label="Local backup location" fullWidth />}
              />
              <Controller
                name="offsiteBackupLocation"
                control={control}
                render={({ field }) => <TextField {...field} label="Off-site backup location" fullWidth />}
              />
            </Stack>

            <Stack direction="row" spacing={3}>
              <Controller
                name="encrypted"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                    label="Encrypted"
                  />
                )}
              />
              <Controller
                name="containsSensitiveData"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                    label="Contains sensitive data"
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="frequency"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Frequency" fullWidth>
                    {BACKUP_FREQUENCIES.map((frequency) => (
                      <MenuItem key={frequency} value={frequency}>
                        {frequency}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <Controller
                name="retention"
                control={control}
                render={({ field }) => <TextField {...field} label="Retention" fullWidth />}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="recoveryPointObjective"
                control={control}
                render={({ field }) => <TextField {...field} label="Recovery point objective" fullWidth />}
              />
              <Controller
                name="recoveryTimeObjective"
                control={control}
                render={({ field }) => <TextField {...field} label="Recovery time objective" fullWidth />}
              />
            </Stack>

            <Controller
              name="lastVerifiedDate"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Last verified date"
                  type="date"
                  fullWidth
                  slotProps={{ inputLabel: { shrink: true } }}
                />
              )}
            />

            <Controller
              name="verificationNotes"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Verification notes" fullWidth multiline minRows={2} />
              )}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          {policy && onDelete && (
            <Button color="error" onClick={onDelete} sx={{ mr: 'auto' }}>
              Delete policy
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
