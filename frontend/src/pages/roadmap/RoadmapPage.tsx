import AddIcon from '@mui/icons-material/Add'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useMemo, useState } from 'react'
import { ApiError } from '../../api/client'
import {
  useCreatePhaseMutation,
  useDeletePhaseMutation,
  useReorderPhasesMutation,
  useUpdatePhaseMutation,
} from '../../api/phases'
import { useCreateProjectMutation, useDeleteProjectMutation, useUpdateProjectMutation } from '../../api/projects'
import { useRoadmapQuery } from '../../api/roadmap'
import type { Phase, Project, ProjectInput, Task, TaskFilters, TaskInput } from '../../api/roadmapTypes'
import {
  useAddDependencyMutation,
  useCreateTaskMutation,
  useDeleteTaskMutation,
  useRemoveDependencyMutation,
  useUpdateTaskMutation,
} from '../../api/tasks'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { ConfirmDialog } from './ConfirmDialog'
import { PhaseFormDialog } from './PhaseFormDialog'
import { ProjectFormDialog } from './ProjectFormDialog'
import { SummaryCards } from './SummaryCards'
import { TaskBoard } from './TaskBoard'
import { TaskFiltersBar } from './TaskFiltersBar'
import { TaskFormDialog } from './TaskFormDialog'
import { TaskTable } from './TaskTable'

function errorMessageOf(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong.'
}

function matchesFilters(task: Task, filters: TaskFilters): boolean {
  if (filters.phaseId && task.phaseId !== filters.phaseId) return false
  if (filters.status && task.status !== filters.status) return false
  if (filters.priority && task.priority !== filters.priority) return false
  if (filters.blocked !== undefined && task.blocked !== filters.blocked) return false
  return true
}

const EMPTY_TASKS: Task[] = []
const EMPTY_PHASES: Phase[] = []

type TaskDialogState = { mode: 'create'; phaseId: string } | { mode: 'edit'; task: Task } | undefined
type PhaseDialogState = { mode: 'create' } | { mode: 'edit'; phase: Phase } | undefined

export function RoadmapPage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()
  const [view, setView] = useState<'board' | 'table'>('board')
  const [filters, setFilters] = useState<TaskFilters>({})

  const [projectDialogOpen, setProjectDialogOpen] = useState(false)
  const [editingProject, setEditingProject] = useState<Project | undefined>(undefined)
  const [deletingProject, setDeletingProject] = useState<Project | undefined>(undefined)

  const [phaseDialog, setPhaseDialog] = useState<PhaseDialogState>(undefined)
  const [deletingPhase, setDeletingPhase] = useState<Phase | undefined>(undefined)

  const [taskDialog, setTaskDialog] = useState<TaskDialogState>(undefined)
  const [deletingTask, setDeletingTask] = useState<Task | undefined>(undefined)

  const [formError, setFormError] = useState<string | undefined>(undefined)

  const { data: roadmap, isPending: roadmapPending, isError: roadmapError } = useRoadmapQuery(selectedProjectId)

  const createProject = useCreateProjectMutation()
  const updateProject = useUpdateProjectMutation()
  const deleteProject = useDeleteProjectMutation()

  const projectId = selectedProjectId ?? ''
  const createPhase = useCreatePhaseMutation(projectId)
  const updatePhase = useUpdatePhaseMutation(projectId)
  const deletePhase = useDeletePhaseMutation(projectId)
  const reorderPhases = useReorderPhasesMutation(projectId)

  const createTaskPhaseId = taskDialog?.mode === 'create' ? taskDialog.phaseId : ''
  const createTask = useCreateTaskMutation(projectId, createTaskPhaseId)
  const updateTask = useUpdateTaskMutation()
  const deleteTask = useDeleteTaskMutation()
  const addDependency = useAddDependencyMutation()
  const removeDependency = useRemoveDependencyMutation()

  const tasks = roadmap?.tasks ?? EMPTY_TASKS
  const phases = roadmap?.phases ?? EMPTY_PHASES

  const tasksByPhase = useMemo(() => {
    const map = new Map<string, Task[]>()
    for (const phase of phases) {
      map.set(
        phase.id,
        tasks.filter((task) => task.phaseId === phase.id && matchesFilters(task, filters)),
      )
    }
    return map
  }, [phases, tasks, filters])

  const filteredTasks = useMemo(() => tasks.filter((task) => matchesFilters(task, filters)), [tasks, filters])

  const phaseById = useMemo(() => new Map(phases.map((phase) => [phase.id, phase])), [phases])

  const editingTask =
    taskDialog?.mode === 'edit' ? (tasks.find((task) => task.id === taskDialog.task.id) ?? taskDialog.task) : undefined

  async function handleCreateOrUpdateProject(input: ProjectInput) {
    setFormError(undefined)
    try {
      if (editingProject) {
        await updateProject.mutateAsync({ projectId: editingProject.id, input })
      } else {
        const created = await createProject.mutateAsync(input)
        setSelectedProjectId(created.id)
      }
      setProjectDialogOpen(false)
      setEditingProject(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDeleteProject() {
    if (!deletingProject) return
    setFormError(undefined)
    try {
      await deleteProject.mutateAsync(deletingProject.id)
      if (selectedProjectId === deletingProject.id) {
        setSelectedProjectId(undefined)
      }
      setDeletingProject(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleCreateOrUpdatePhase(input: { name: string; description: string | null }) {
    setFormError(undefined)
    try {
      if (phaseDialog?.mode === 'edit') {
        await updatePhase.mutateAsync({ phaseId: phaseDialog.phase.id, input })
      } else {
        await createPhase.mutateAsync(input)
      }
      setPhaseDialog(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDeletePhase() {
    if (!deletingPhase) return
    setFormError(undefined)
    try {
      await deletePhase.mutateAsync(deletingPhase.id)
      setDeletingPhase(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleMovePhase(phase: Phase, direction: 'left' | 'right') {
    const index = phases.findIndex((candidate) => candidate.id === phase.id)
    const swapIndex = direction === 'left' ? index - 1 : index + 1
    if (swapIndex < 0 || swapIndex >= phases.length) return
    const orderedIds = phases.map((candidate) => candidate.id)
    const [moved] = orderedIds.splice(index, 1)
    if (!moved) return
    orderedIds.splice(swapIndex, 0, moved)
    await reorderPhases.mutateAsync(orderedIds)
  }

  async function handleCreateOrUpdateTask(input: TaskInput) {
    setFormError(undefined)
    try {
      if (taskDialog?.mode === 'edit') {
        await updateTask.mutateAsync({ taskId: taskDialog.task.id, input })
      } else if (taskDialog?.mode === 'create') {
        await createTask.mutateAsync(input)
      }
      setTaskDialog(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDeleteTask() {
    if (!deletingTask) return
    setFormError(undefined)
    try {
      await deleteTask.mutateAsync(deletingTask.id)
      setDeletingTask(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  if (projectsPending) {
    return (
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
        <CircularProgress size={20} />
        <Typography>Loading projects…</Typography>
      </Stack>
    )
  }

  if (projectsError) {
    return <Alert severity="error">Unable to load projects.</Alert>
  }

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
        <Typography variant="h4" component="h2">
          Roadmap
        </Typography>
        <Button
          startIcon={<AddIcon />}
          variant="contained"
          onClick={() => {
            setEditingProject(undefined)
            setFormError(undefined)
            setProjectDialogOpen(true)
          }}
        >
          New project
        </Button>
      </Stack>

      {projects && projects.length === 0 && (
        <Alert severity="info">No projects yet. Create one to start planning your roadmap.</Alert>
      )}

      {projects && projects.length > 0 && (
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'center' } }}>
          <TextField
            select
            label="Project"
            value={selectedProjectId ?? ''}
            onChange={(event) => setSelectedProjectId(event.target.value)}
            sx={{ minWidth: 260 }}
          >
            {projects.map((project) => (
              <MenuItem key={project.id} value={project.id}>
                {project.name}
              </MenuItem>
            ))}
          </TextField>
          {roadmap && (
            <Stack direction="row" spacing={1}>
              <Button
                size="small"
                onClick={() => {
                  setEditingProject(roadmap.project)
                  setFormError(undefined)
                  setProjectDialogOpen(true)
                }}
              >
                Edit project
              </Button>
              <Button
                size="small"
                color="error"
                onClick={() => {
                  setFormError(undefined)
                  setDeletingProject(roadmap.project)
                }}
              >
                Delete project
              </Button>
            </Stack>
          )}
        </Stack>
      )}

      {roadmapPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading roadmap…</Typography>
        </Stack>
      )}

      {roadmapError && <Alert severity="error">Unable to load this project's roadmap.</Alert>}

      {roadmap && (
        <>
          <SummaryCards project={roadmap.project} />

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
            <Typography variant="h5" component="h3">
              Phases
            </Typography>
            <Button
              startIcon={<AddIcon />}
              onClick={() => {
                setFormError(undefined)
                setPhaseDialog({ mode: 'create' })
              }}
            >
              New phase
            </Button>
          </Stack>

          <TaskFiltersBar phases={phases} filters={filters} onChange={setFilters} />

          <Tabs value={view} onChange={(_event, value) => setView(value)} aria-label="Roadmap view">
            <Tab label="Board" value="board" />
            <Tab label="Table" value="table" />
          </Tabs>

          <Box role="tabpanel" hidden={view !== 'board'}>
            {view === 'board' && (
              <TaskBoard
                phases={phases}
                tasksByPhase={tasksByPhase}
                onAddTask={(phaseId) => {
                  setFormError(undefined)
                  setTaskDialog({ mode: 'create', phaseId })
                }}
                onEditTask={(task) => {
                  setFormError(undefined)
                  setTaskDialog({ mode: 'edit', task })
                }}
                onEditPhase={(phase) => {
                  setFormError(undefined)
                  setPhaseDialog({ mode: 'edit', phase })
                }}
                onDeletePhase={(phase) => {
                  setFormError(undefined)
                  setDeletingPhase(phase)
                }}
                onMovePhase={handleMovePhase}
              />
            )}
          </Box>

          <Box role="tabpanel" hidden={view !== 'table'}>
            {view === 'table' && (
              <TaskTable
                tasks={filteredTasks}
                phaseById={phaseById}
                onEditTask={(task) => {
                  setFormError(undefined)
                  setTaskDialog({ mode: 'edit', task })
                }}
              />
            )}
          </Box>
        </>
      )}

      <ProjectFormDialog
        open={projectDialogOpen}
        project={editingProject}
        onClose={() => setProjectDialogOpen(false)}
        onSubmit={handleCreateOrUpdateProject}
        errorMessage={formError}
      />

      <ConfirmDialog
        open={Boolean(deletingProject)}
        title="Delete project"
        description={`Delete "${deletingProject?.name}"? This is only possible while it has no phases.`}
        errorMessage={formError}
        confirming={deleteProject.isPending}
        onCancel={() => setDeletingProject(undefined)}
        onConfirm={handleDeleteProject}
      />

      <PhaseFormDialog
        open={Boolean(phaseDialog)}
        phase={phaseDialog?.mode === 'edit' ? phaseDialog.phase : undefined}
        onClose={() => setPhaseDialog(undefined)}
        onSubmit={handleCreateOrUpdatePhase}
        errorMessage={formError}
      />

      <ConfirmDialog
        open={Boolean(deletingPhase)}
        title="Delete phase"
        description={`Delete "${deletingPhase?.name}"? This is only possible while it has no tasks.`}
        errorMessage={formError}
        confirming={deletePhase.isPending}
        onCancel={() => setDeletingPhase(undefined)}
        onConfirm={handleDeletePhase}
      />

      {taskDialog && (
        <TaskFormDialog
          open={Boolean(taskDialog)}
          task={editingTask}
          phaseName={
            taskDialog.mode === 'create'
              ? (phaseById.get(taskDialog.phaseId)?.name ?? '')
              : (phaseById.get(taskDialog.task.phaseId)?.name ?? '')
          }
          allTasks={tasks}
          onClose={() => setTaskDialog(undefined)}
          onSubmit={handleCreateOrUpdateTask}
          onAddDependency={(dependsOnTaskId) =>
            taskDialog.mode === 'edit'
              ? addDependency.mutateAsync({ taskId: taskDialog.task.id, dependsOnTaskId })
              : Promise.resolve()
          }
          onRemoveDependency={(dependsOnTaskId) =>
            taskDialog.mode === 'edit'
              ? removeDependency.mutateAsync({ taskId: taskDialog.task.id, dependsOnTaskId })
              : Promise.resolve()
          }
          onDelete={
            taskDialog.mode === 'edit'
              ? () => {
                  setDeletingTask(taskDialog.task)
                  setTaskDialog(undefined)
                }
              : undefined
          }
          errorMessage={formError}
        />
      )}

      <ConfirmDialog
        open={Boolean(deletingTask)}
        title="Delete task"
        description={`Delete "${deletingTask?.title}"? This cannot be undone.`}
        errorMessage={formError}
        confirming={deleteTask.isPending}
        onCancel={() => setDeletingTask(undefined)}
        onConfirm={handleDeleteTask}
      />
    </Stack>
  )
}
