import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemText from '@mui/material/ListItemText'
import Typography from '@mui/material/Typography'
import type { RecommendedAction } from '../../api/dashboardTypes'

interface RecommendedActionsListProps {
  actions: RecommendedAction[]
}

export function RecommendedActionsList({ actions }: RecommendedActionsListProps) {
  if (actions.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        Nothing needs attention right now.
      </Typography>
    )
  }

  return (
    <List dense disablePadding>
      {actions.map((action, index) => (
        <ListItem key={`${action.category}-${index}`} disableGutters>
          <ListItemText primary={action.message} />
        </ListItem>
      ))}
    </List>
  )
}
