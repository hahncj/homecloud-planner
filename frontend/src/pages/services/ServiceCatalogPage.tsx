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
import type { ManagedServiceFilters, ManagedServiceInput } from '../../api/serviceCatalogTypes'
import { useCreateServiceMutation, useServicesQuery } from '../../api/services'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { ServiceCard } from './ServiceCard'
import { ServiceFiltersBar } from './ServiceFiltersBar'
import { ServiceFormDialog } from './ServiceFormDialog'

export function ServiceCatalogPage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()
  const navigate = useNavigate()

  const [filters, setFilters] = useState<ManagedServiceFilters>({})
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const projectId = selectedProjectId ?? ''
  const { data: services, isPending: servicesPending, isError: servicesError } = useServicesQuery(selectedProjectId, filters)
  const { data: devices } = useDevicesQuery(selectedProjectId)
  const createService = useCreateServiceMutation(projectId)

  async function handleCreateService(input: ManagedServiceInput) {
    setFormError(undefined)
    try {
      const created = await createService.mutateAsync(input)
      setCreateDialogOpen(false)
      navigate(`/services/${created.id}`)
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
          Service Catalog
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
          New service
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

      {servicesPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading services…</Typography>
        </Stack>
      )}

      {servicesError && <Alert severity="error">Unable to load services.</Alert>}

      {selectedProjectId && (
        <>
          <ServiceFiltersBar filters={filters} onChange={setFilters} />

          {services && services.length === 0 && (
            <Typography color="text.secondary">No services match the current filters.</Typography>
          )}

          {services && services.length > 0 && (
            <Grid container spacing={2}>
              {services.map((service) => (
                <Grid key={service.id} size={{ xs: 12, sm: 6, md: 4 }}>
                  <ServiceCard service={service} onClick={() => navigate(`/services/${service.id}`)} />
                </Grid>
              ))}
            </Grid>
          )}
        </>
      )}

      <ServiceFormDialog
        open={createDialogOpen}
        devices={devices ?? []}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateService}
        errorMessage={formError}
      />
    </Stack>
  )
}
