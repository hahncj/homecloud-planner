import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import Chip from '@mui/material/Chip'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import type { Phase, Task } from '../../api/roadmapTypes'
import { priorityColor, statusColor } from './statusColors'

interface TaskTableProps {
  tasks: Task[]
  phaseById: Map<string, Phase>
  onEditTask: (task: Task) => void
}

export function TaskTable({ tasks, phaseById, onEditTask }: TaskTableProps) {
  if (tasks.length === 0) {
    return <Typography color="text.secondary">No tasks match the current filters.</Typography>
  }

  return (
    <TableContainer sx={{ overflowX: 'auto' }}>
      <Table size="small" aria-label="Tasks">
        <TableHead>
          <TableRow>
            <TableCell>Title</TableCell>
            <TableCell>Phase</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Priority</TableCell>
            <TableCell>Target date</TableCell>
            <TableCell align="right">Estimated</TableCell>
            <TableCell align="right">Actual</TableCell>
            <TableCell>Blocked</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tasks.map((task) => (
            <TableRow
              key={task.id}
              hover
              onClick={() => onEditTask(task)}
              sx={{ cursor: 'pointer' }}
              tabIndex={0}
              onKeyDown={(event) => {
                if (event.key === 'Enter') onEditTask(task)
              }}
            >
              <TableCell>{task.title}</TableCell>
              <TableCell>{phaseById.get(task.phaseId)?.name ?? '—'}</TableCell>
              <TableCell>
                <Chip size="small" label={task.status.replaceAll('_', ' ')} color={statusColor(task.status)} />
              </TableCell>
              <TableCell>
                <Chip size="small" label={task.priority} color={priorityColor(task.priority)} variant="outlined" />
              </TableCell>
              <TableCell>{task.targetDate ?? '—'}</TableCell>
              <TableCell align="right">{task.estimatedCost ?? '—'}</TableCell>
              <TableCell align="right">{task.actualCost ?? '—'}</TableCell>
              <TableCell>
                {task.blocked && (
                  <Chip size="small" icon={<WarningAmberIcon />} label="Blocked" color="error" variant="outlined" />
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
