import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Divider from '@mui/material/Divider'
import Grid from '@mui/material/Grid'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { Device } from '../../api/deviceTypes'
import { warrantyColor, warrantyLabel, warrantyState } from '../shared/warrantyStatus'
import { lifecycleColor } from './lifecycleColors'

interface DeviceDetailDialogProps {
  open: boolean
  device: Device | undefined
  onClose: () => void
  onEdit: () => void
}

function Field({ label, value }: { label: string; value: string | number | null }) {
  return (
    <Grid size={{ xs: 12, sm: 6 }}>
      <Typography variant="overline" color="text.secondary" sx={{ display: 'block' }}>
        {label}
      </Typography>
      <Typography variant="body1">{value ?? '—'}</Typography>
    </Grid>
  )
}

export function DeviceDetailDialog({ open, device, onClose, onEdit }: DeviceDetailDialogProps) {
  if (!device) return null
  const state = warrantyState(device.warrantyExpiration)

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{device.name}</DialogTitle>
      <DialogContent>
        <Stack direction="row" spacing={1} sx={{ mb: 2, flexWrap: 'wrap' }}>
          <Chip label={device.lifecycleStatus} color={lifecycleColor(device.lifecycleStatus)} />
          {state !== 'none' && (
            <Chip label={warrantyLabel(state)} color={warrantyColor(state)} variant="outlined" />
          )}
        </Stack>

        <Grid container spacing={2}>
          <Field label="Manufacturer" value={device.manufacturer} />
          <Field label="Model" value={device.model} />
          <Field label="Serial number" value={device.serialNumber} />
          <Field label="Role" value={device.role} />
          <Field label="Location" value={device.location} />
          <Field label="Hostname" value={device.hostname} />
          <Field label="IP address" value={device.ipAddress} />
          <Field label="MAC address" value={device.macAddress} />
          <Field label="VLAN" value={device.vlan} />
          <Field label="Operating system" value={device.operatingSystem} />
          <Field label="Firmware version" value={device.firmwareVersion} />
          <Field label="Purchase date" value={device.purchaseDate} />
          <Field label="Warranty expiration" value={device.warrantyExpiration} />
          <Field label="Replacement target" value={device.replacementTarget} />
        </Grid>

        {device.notes && (
          <>
            <Divider sx={{ my: 2 }} />
            <Typography variant="overline" color="text.secondary" sx={{ display: 'block' }}>
              Notes
            </Typography>
            <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
              {device.notes}
            </Typography>
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
        <Button variant="contained" onClick={onEdit}>
          Edit
        </Button>
      </DialogActions>
    </Dialog>
  )
}
