import AddIcon from '@mui/icons-material/Add'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import ArrowForwardIcon from '@mui/icons-material/ArrowForward'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutlined'
import EditIcon from '@mui/icons-material/Edit'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import IconButton from '@mui/material/IconButton'
import LinearProgress from '@mui/material/LinearProgress'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import type { Phase, Task } from '../../api/roadmapTypes'
import { priorityColor, statusColor } from './statusColors'

interface TaskBoardProps {
  phases: Phase[]
  tasksByPhase: Map<string, Task[]>
  onAddTask: (phaseId: string) => void
  onEditTask: (task: Task) => void
  onEditPhase: (phase: Phase) => void
  onDeletePhase: (phase: Phase) => void
  onMovePhase: (phase: Phase, direction: 'left' | 'right') => void
}

export function TaskBoard({
  phases,
  tasksByPhase,
  onAddTask,
  onEditTask,
  onEditPhase,
  onDeletePhase,
  onMovePhase,
}: TaskBoardProps) {
  if (phases.length === 0) {
    return <Typography color="text.secondary">Add a phase to start planning tasks.</Typography>
  }

  return (
    <Box sx={{ display: 'flex', gap: 2, overflowX: 'auto', pb: 1 }}>
      {phases.map((phase, index) => {
        const tasks = tasksByPhase.get(phase.id) ?? []
        return (
          <Paper key={phase.id} variant="outlined" sx={{ minWidth: 280, width: 280, flexShrink: 0, p: 2 }}>
            <Stack spacing={1}>
              <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="subtitle1" component="h3">
                    {phase.sequence}. {phase.name}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {phase.progress.completedCount}/{phase.progress.taskCount} complete
                  </Typography>
                </Box>
                <Stack direction="row">
                  <Tooltip title="Move phase earlier">
                    <span>
                      <IconButton
                        size="small"
                        aria-label={`Move ${phase.name} earlier`}
                        disabled={index === 0}
                        onClick={() => onMovePhase(phase, 'left')}
                      >
                        <ArrowBackIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>
                  <Tooltip title="Move phase later">
                    <span>
                      <IconButton
                        size="small"
                        aria-label={`Move ${phase.name} later`}
                        disabled={index === phases.length - 1}
                        onClick={() => onMovePhase(phase, 'right')}
                      >
                        <ArrowForwardIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>
                  <Tooltip title="Edit phase">
                    <IconButton size="small" aria-label={`Edit ${phase.name}`} onClick={() => onEditPhase(phase)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete phase">
                    <IconButton size="small" aria-label={`Delete ${phase.name}`} onClick={() => onDeletePhase(phase)}>
                      <DeleteOutlineIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Stack>
              </Stack>

              <LinearProgress
                variant="determinate"
                value={phase.progress.progressPercentage}
                aria-label={`${phase.name} progress`}
                sx={{ height: 6, borderRadius: 1 }}
              />

              <Stack spacing={1} sx={{ mt: 1 }}>
                {tasks.length === 0 && (
                  <Typography variant="body2" color="text.secondary">
                    No tasks yet.
                  </Typography>
                )}
                {tasks.map((task) => (
                  <Card key={task.id} variant="outlined">
                    <CardActionArea onClick={() => onEditTask(task)} aria-label={`Edit ${task.title}`}>
                      <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {task.title}
                        </Typography>
                        <Stack direction="row" spacing={0.5} sx={{ mt: 0.5, flexWrap: 'wrap' }}>
                          <Chip size="small" label={task.status.replaceAll('_', ' ')} color={statusColor(task.status)} />
                          <Chip size="small" label={task.priority} color={priorityColor(task.priority)} variant="outlined" />
                          {task.blocked && (
                            <Chip
                              size="small"
                              icon={<WarningAmberIcon />}
                              label="Blocked"
                              color="error"
                              variant="outlined"
                            />
                          )}
                        </Stack>
                      </CardContent>
                    </CardActionArea>
                  </Card>
                ))}
                <IconButton
                  size="small"
                  aria-label={`Add task to ${phase.name}`}
                  onClick={() => onAddTask(phase.id)}
                  sx={{ alignSelf: 'flex-start' }}
                >
                  <AddIcon fontSize="small" />
                </IconButton>
              </Stack>
            </Stack>
          </Paper>
        )
      })}
    </Box>
  )
}
