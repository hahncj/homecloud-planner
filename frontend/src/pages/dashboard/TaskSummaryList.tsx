import Chip from '@mui/material/Chip'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemText from '@mui/material/ListItemText'
import Typography from '@mui/material/Typography'
import type { TaskSummary } from '../../api/dashboardTypes'

interface TaskSummaryListProps {
  tasks: TaskSummary[]
  emptyMessage: string
  dateLabel: 'target' | 'completed'
}

export function TaskSummaryList({ tasks, emptyMessage, dateLabel }: TaskSummaryListProps) {
  if (tasks.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        {emptyMessage}
      </Typography>
    )
  }

  return (
    <List dense disablePadding>
      {tasks.map((task) => {
        const date = dateLabel === 'target' ? task.targetDate : task.completedDate
        return (
          <ListItem key={task.id} disableGutters>
            <ListItemText
              primary={task.title}
              secondary={`${task.phaseName}${date ? ` · ${date}` : ''}`}
            />
            {task.blocked && <Chip size="small" label="Blocked" color="error" variant="outlined" />}
          </ListItem>
        )
      })}
    </List>
  )
}
