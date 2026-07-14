import AddIcon from '@mui/icons-material/Add'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDevicesQuery } from '../../api/devices'
import type { ArchitectureDecisionFilters, ArchitectureDecisionInput } from '../../api/decisionTypes'
import { DECISION_STATUSES } from '../../api/decisionTypes'
import { useCreateDecisionMutation, useDecisionsQuery } from '../../api/decisions'
import { useServicesQuery } from '../../api/services'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { DecisionCard } from './DecisionCard'
import { DecisionFormDialog } from './DecisionFormDialog'

export function DecisionsPage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()
  const navigate = useNavigate()

  const [filters, setFilters] = useState<ArchitectureDecisionFilters>({})
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const projectId = selectedProjectId ?? ''
  const { data: decisions, isPending: decisionsPending, isError: decisionsError } = useDecisionsQuery(
    selectedProjectId,
    filters,
  )
  const { data: devices } = useDevicesQuery(selectedProjectId)
  const { data: services } = useServicesQuery(selectedProjectId)
  const createDecision = useCreateDecisionMutation(projectId)

  async function handleCreateDecision(input: ArchitectureDecisionInput) {
    setFormError(undefined)
    try {
      const created = await createDecision.mutateAsync(input)
      setCreateDialogOpen(false)
      navigate(`/decisions/${created.id}`)
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Something went wrong.')
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
          Architecture Decisions
        </Typography>
        <Button
          startIcon={<AddIcon />}
          variant="contained"
          onClick={() => {
            setFormError(undefined)
            setCreateDialogOpen(true)
          }}
          disabled={!selectedProjectId}
        >
          New decision
        </Button>
      </Stack>

      {projects && projects.length === 0 && (
        <Alert severity="info">No projects yet. Create one from the Roadmap page first.</Alert>
      )}

      {projects && projects.length > 0 && (
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
      )}

      {decisionsPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading decisions…</Typography>
        </Stack>
      )}

      {decisionsError && <Alert severity="error">Unable to load decisions.</Alert>}

      {selectedProjectId && (
        <>
          <TextField
            select
            size="small"
            label="Status"
            value={filters.status ?? ''}
            onChange={(event) => setFilters({ ...filters, status: (event.target.value || undefined) as never })}
            sx={{ minWidth: 160 }}
          >
            <MenuItem value="">All statuses</MenuItem>
            {DECISION_STATUSES.map((status) => (
              <MenuItem key={status} value={status}>
                {status}
              </MenuItem>
            ))}
          </TextField>

          {decisions && decisions.length === 0 && (
            <Typography color="text.secondary">No decisions match the current filters.</Typography>
          )}

          {decisions && decisions.length > 0 && (
            <Grid container spacing={2}>
              {decisions.map((decision) => (
                <Grid key={decision.id} size={{ xs: 12, sm: 6, md: 4 }}>
                  <DecisionCard decision={decision} onClick={() => navigate(`/decisions/${decision.id}`)} />
                </Grid>
              ))}
            </Grid>
          )}
        </>
      )}

      <DecisionFormDialog
        open={createDialogOpen}
        devices={devices ?? []}
        services={services ?? []}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateDecision}
        errorMessage={formError}
      />
    </Stack>
  )
}
