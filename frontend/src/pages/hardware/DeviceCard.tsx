import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { Device } from '../../api/deviceTypes'
import { warrantyColor, warrantyLabel, warrantyState } from '../shared/warrantyStatus'
import { lifecycleColor } from './lifecycleColors'

interface DeviceCardProps {
  device: Device
  onClick: () => void
}

export function DeviceCard({ device, onClick }: DeviceCardProps) {
  const state = warrantyState(device.warrantyExpiration)

  return (
    <Card variant="outlined">
      <CardActionArea onClick={onClick} aria-label={`View ${device.name}`}>
        <CardContent>
          <Typography variant="subtitle1" component="h3">
            {device.name}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {[device.manufacturer, device.model].filter(Boolean).join(' ') || 'No manufacturer/model on file'}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {device.location ?? 'No location on file'}
          </Typography>
          <Stack direction="row" spacing={0.5} sx={{ mt: 1, flexWrap: 'wrap' }}>
            <Chip size="small" label={device.lifecycleStatus} color={lifecycleColor(device.lifecycleStatus)} />
            {device.role && <Chip size="small" label={device.role} variant="outlined" />}
            {state !== 'none' && (
              <Chip size="small" label={warrantyLabel(state)} color={warrantyColor(state)} variant="outlined" />
            )}
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
