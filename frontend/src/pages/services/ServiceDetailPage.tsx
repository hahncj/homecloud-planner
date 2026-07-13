import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import LockIcon from '@mui/icons-material/Lock'
import PublicIcon from '@mui/icons-material/Public'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Divider from '@mui/material/Divider'
import Grid from '@mui/material/Grid'
import Link from '@mui/material/Link'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { useDevicesQuery } from '../../api/devices'
import type { ManagedServiceInput } from '../../api/serviceCatalogTypes'
import {
  useAddServiceDependencyMutation,
  useDeleteServiceMutation,
  useRemoveServiceDependencyMutation,
  useServiceQuery,
  useServicesQuery,
  useUpdateServiceMutation,
} from '../../api/services'
import { ConfirmDialog } from '../roadmap/ConfirmDialog'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { ServiceDependencySection } from './ServiceDependencySection'
import { ServiceFormDialog } from './ServiceFormDialog'
import { sensitivityColor, serviceStatusColor } from './statusColors'

function errorMessageOf(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong.'
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Grid size={{ xs: 12, sm: 6 }}>
      <Typography variant="overline" color="text.secondary" sx={{ display: 'block' }}>
        {label}
      </Typography>
      <Typography variant="body1">{value ?? '—'}</Typography>
    </Grid>
  )
}

export function ServiceDetailPage() {
  const { serviceId } = useParams<{ serviceId: string }>()
  const navigate = useNavigate()
  const { setSelectedProjectId } = useSelectedProject()

  const { data: service, isPending, isError } = useServiceQuery(serviceId)
  const { data: allServices } = useServicesQuery(service?.projectId)
  const { data: devices } = useDevicesQuery(service?.projectId)

  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const updateService = useUpdateServiceMutation()
  const deleteService = useDeleteServiceMutation()
  const addDependency = useAddServiceDependencyMutation()
  const removeDependency = useRemoveServiceDependencyMutation()

  useEffect(() => {
    if (service) {
      setSelectedProjectId(service.projectId)
    }
  }, [service, setSelectedProjectId])

  async function handleUpdate(input: ManagedServiceInput) {
    if (!serviceId) return
    setFormError(undefined)
    try {
      await updateService.mutateAsync({ serviceId, input })
      setEditDialogOpen(false)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDelete() {
    if (!serviceId) return
    setFormError(undefined)
    try {
      await deleteService.mutateAsync(serviceId)
      navigate('/services')
    } catch (error) {
      setFormError(errorMessageOf(error))
      setDeleteDialogOpen(false)
    }
  }

  if (isPending) {
    return (
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
        <CircularProgress size={20} />
        <Typography>Loading service…</Typography>
      </Stack>
    )
  }

  if (isError || !service) {
    return <Alert severity="error">Unable to load this service.</Alert>
  }

  const hostDevice = devices?.find((device) => device.id === service.hostDeviceId)

  return (
    <Stack spacing={3}>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/services')} sx={{ alignSelf: 'flex-start' }}>
        Back to catalog
      </Button>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
        <Typography variant="h4" component="h2">
          {service.name}
        </Typography>
        <Stack direction="row" spacing={1}>
          <Button
            variant="outlined"
            onClick={() => {
              setFormError(undefined)
              setEditDialogOpen(true)
            }}
          >
            Edit
          </Button>
          <Button
            variant="outlined"
            color="error"
            onClick={() => {
              setFormError(undefined)
              setDeleteDialogOpen(true)
            }}
          >
            Delete
          </Button>
        </Stack>
      </Stack>

      {formError && !editDialogOpen && !deleteDialogOpen && <Alert severity="error">{formError}</Alert>}

      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
        <Chip label={service.status} color={serviceStatusColor(service.status)} />
        <Chip label={service.runtimeType.replaceAll('_', ' ')} variant="outlined" />
        <Chip
          label={service.sensitivity.replaceAll('_', ' ')}
          color={sensitivityColor(service.sensitivity)}
          variant="outlined"
        />
        <Chip
          icon={service.externallyExposed ? <PublicIcon /> : <LockIcon />}
          label={service.externallyExposed ? 'Externally exposed' : 'Internal only'}
          variant="outlined"
        />
      </Stack>

      {service.purpose && <Typography variant="body1">{service.purpose}</Typography>}
      {service.description && (
        <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-wrap' }}>
          {service.description}
        </Typography>
      )}

      <Divider />

      <Grid container spacing={2}>
        <Field label="Host device" value={hostDevice?.name ?? (service.hostDeviceId ? 'Unknown device' : null)} />
        <Field label="Storage location" value={service.storageLocation} />
        <Field label="Authentication method" value={service.authenticationMethod} />
        <Field label="Backup policy" value={service.backupPolicy} />
        <Field
          label="Documentation"
          value={
            service.documentationUrl ? (
              <Link href={service.documentationUrl} target="_blank" rel="noopener noreferrer">
                {service.documentationUrl}
              </Link>
            ) : null
          }
        />
        <Field
          label="Repository"
          value={
            service.repositoryUrl ? (
              <Link href={service.repositoryUrl} target="_blank" rel="noopener noreferrer">
                {service.repositoryUrl}
              </Link>
            ) : null
          }
        />
      </Grid>

      {service.notes && (
        <>
          <Divider />
          <Typography variant="overline" color="text.secondary" sx={{ display: 'block' }}>
            Notes
          </Typography>
          <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
            {service.notes}
          </Typography>
        </>
      )}

      <Divider />

      <Typography variant="h6" component="h3">
        Dependencies
      </Typography>
      <ServiceDependencySection
        service={service}
        allServices={allServices ?? []}
        onAdd={(dependsOnServiceId) => addDependency.mutateAsync({ serviceId: service.id, dependsOnServiceId })}
        onRemove={(dependsOnServiceId) => removeDependency.mutateAsync({ serviceId: service.id, dependsOnServiceId })}
      />

      <ServiceFormDialog
        open={editDialogOpen}
        service={service}
        devices={devices ?? []}
        onClose={() => setEditDialogOpen(false)}
        onSubmit={handleUpdate}
        errorMessage={formError}
      />

      <ConfirmDialog
        open={deleteDialogOpen}
        title="Delete service"
        description={`Delete "${service.name}"? This is only possible while no other service depends on it.`}
        errorMessage={formError}
        confirming={deleteService.isPending}
        onCancel={() => setDeleteDialogOpen(false)}
        onConfirm={handleDelete}
      />
    </Stack>
  )
}
