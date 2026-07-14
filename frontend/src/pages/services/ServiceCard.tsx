import LockIcon from '@mui/icons-material/Lock'
import PublicIcon from '@mui/icons-material/Public'
import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { ManagedService } from '../../api/serviceCatalogTypes'
import { sensitivityColor, serviceStatusColor } from './statusColors'

interface ServiceCardProps {
  service: ManagedService
  onClick: () => void
}

export function ServiceCard({ service, onClick }: ServiceCardProps) {
  return (
    <Card variant="outlined">
      <CardActionArea onClick={onClick} aria-label={`View ${service.name}`}>
        <CardContent>
          <Typography variant="subtitle1" component="h3">
            {service.name}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {service.purpose ?? 'No purpose on file'}
          </Typography>
          <Stack direction="row" spacing={0.5} sx={{ mt: 1, flexWrap: 'wrap' }}>
            <Chip size="small" label={service.status} color={serviceStatusColor(service.status)} />
            <Chip size="small" label={service.runtimeType.replaceAll('_', ' ')} variant="outlined" />
            <Chip
              size="small"
              label={service.sensitivity.replaceAll('_', ' ')}
              color={sensitivityColor(service.sensitivity)}
              variant="outlined"
            />
            <Chip
              size="small"
              icon={service.externallyExposed ? <PublicIcon /> : <LockIcon />}
              label={service.externallyExposed ? 'External' : 'Internal only'}
              variant="outlined"
            />
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
