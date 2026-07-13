import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemText from '@mui/material/ListItemText'
import Typography from '@mui/material/Typography'
import type { WarrantyExpirationSummary } from '../../api/dashboardTypes'

interface WarrantyExpirationsListProps {
  items: WarrantyExpirationSummary[]
}

export function WarrantyExpirationsList({ items }: WarrantyExpirationsListProps) {
  if (items.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No warranties expiring in the next 30 days.
      </Typography>
    )
  }

  return (
    <List dense disablePadding>
      {items.map((item) => (
        <ListItem key={`${item.entityType}-${item.id}`} disableGutters>
          <ListItemText
            primary={item.name}
            secondary={`${item.entityType === 'DEVICE' ? 'Device' : 'Purchase item'} · expires ${item.warrantyExpiration}`}
          />
        </ListItem>
      ))}
    </List>
  )
}
