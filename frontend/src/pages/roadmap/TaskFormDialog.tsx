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
import type { Task, TaskInput } from '../../api/roadmapTypes'
import { TASK_PRIORITIES, TASK_STATUSES } from '../../api/roadmapTypes'
import { DependencySection } from './DependencySection'
import { taskFormSchema } from './schemas'
import type { TaskFormInput, TaskFormOutput } from './schemas'

interface TaskFormDialogProps {
  open: boolean
  task?: Task
  phaseName: string
  allTasks: Task[]
  onClose: () => void
  onSubmit: (input: TaskInput) => Promise<unknown>
  onAddDependency: (dependsOnTaskId: string) => Promise<unknown>
  onRemoveDependency: (dependsOnTaskId: string) => Promise<unknown>
  onDelete?: () => void
  errorMessage?: string
}

function toDefaultValues(task?: Task): TaskFormInput {
  return {
    title: task?.title ?? '',
    description: task?.description ?? '',
    status: task?.status ?? 'NOT_STARTED',
    priority: task?.priority ?? 'MEDIUM',
    estimatedCost: task?.estimatedCost ?? '',
    actualCost: task?.actualCost ?? '',
    targetDate: task?.targetDate ?? '',
    completedDate: task?.completedDate ?? '',
    acceptanceCriteria: task?.acceptanceCriteria ?? '',
    notes: task?.notes ?? '',
  }
}

export function TaskFormDialog({
  open,
  task,
  phaseName,
  allTasks,
  onClose,
  onSubmit,
  onAddDependency,
  onRemoveDependency,
  onDelete,
  errorMessage,
}: TaskFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<TaskFormInput, unknown, TaskFormOutput>({
    resolver: zodResolver(taskFormSchema),
    values: toDefaultValues(task),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit({
      title: values.title,
      description: values.description,
      status: values.status,
      priority: values.priority,
      estimatedCost: values.estimatedCost,
      actualCost: values.actualCost,
      targetDate: values.targetDate,
      completedDate: values.completedDate,
      acceptanceCriteria: values.acceptanceCriteria,
      notes: values.notes,
    })
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{task ? 'Edit task' : `New task in ${phaseName}`}</DialogTitle>
      <form onSubmit={submit} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            <Controller
              name="title"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Title"
                  required
                  fullWidth
                  autoFocus
                  error={Boolean(errors.title)}
                  helperText={errors.title?.message}
                />
              )}
            />

            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Description" fullWidth multiline minRows={2} />
              )}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="status"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Status" fullWidth>
                    {TASK_STATUSES.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status.replaceAll('_', ' ')}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <Controller
                name="priority"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Priority" fullWidth>
                    {TASK_PRIORITIES.map((priority) => (
                      <MenuItem key={priority} value={priority}>
                        {priority}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="estimatedCost"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Estimated cost"
                    type="number"
                    fullWidth
                    slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                    error={Boolean(errors.estimatedCost)}
                    helperText={errors.estimatedCost?.message}
                  />
                )}
              />
              <Controller
                name="actualCost"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Actual cost"
                    type="number"
                    fullWidth
                    slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                    error={Boolean(errors.actualCost)}
                    helperText={errors.actualCost?.message}
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="targetDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Target date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
              <Controller
                name="completedDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Completed date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
            </Stack>

            <Controller
              name="acceptanceCriteria"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Acceptance criteria" fullWidth multiline minRows={2} />
              )}
            />

            <Controller
              name="notes"
              control={control}
              render={({ field }) => <TextField {...field} label="Notes" fullWidth multiline minRows={2} />}
            />

            {task && (
              <DependencySection
                task={task}
                allTasks={allTasks}
                onAdd={onAddDependency}
                onRemove={onRemoveDependency}
              />
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          {task && onDelete && (
            <Button color="error" onClick={onDelete} sx={{ mr: 'auto' }}>
              Delete task
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
