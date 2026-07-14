import AddIcon from '@mui/icons-material/Add'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useMemo, useState } from 'react'
import { ApiError } from '../../api/client'
import type { Device, DeviceFilters, DeviceInput } from '../../api/deviceTypes'
import { useCreateDeviceMutation, useDeleteDeviceMutation, useDevicesQuery, useUpdateDeviceMutation } from '../../api/devices'
import { ConfirmDialog } from '../roadmap/ConfirmDialog'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { DeviceCard } from './DeviceCard'
import { DeviceDetailDialog } from './DeviceDetailDialog'
import { DeviceFiltersBar } from './DeviceFiltersBar'
import { DeviceFormDialog } from './DeviceFormDialog'
import { DeviceTable } from './DeviceTable'

function errorMessageOf(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong.'
}

const EMPTY_DEVICES: Device[] = []

export function HardwarePage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()

  const [view, setView] = useState<'cards' | 'table'>('cards')
  const [filters, setFilters] = useState<DeviceFilters>({})
  const [formDialogOpen, setFormDialogOpen] = useState(false)
  const [editingDevice, setEditingDevice] = useState<Device | undefined>(undefined)
  const [viewingDevice, setViewingDevice] = useState<Device | undefined>(undefined)
  const [deletingDevice, setDeletingDevice] = useState<Device | undefined>(undefined)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const projectId = selectedProjectId ?? ''
  const { data: allDevices, isPending: devicesPending, isError: devicesError } = useDevicesQuery(selectedProjectId)
  const { data: filteredDevices } = useDevicesQuery(selectedProjectId, filters)

  const createDevice = useCreateDeviceMutation(projectId)
  const updateDevice = useUpdateDeviceMutation()
  const deleteDevice = useDeleteDeviceMutation()

  const devices = allDevices ?? EMPTY_DEVICES
  const roles = useMemo(
    () => Array.from(new Set(devices.map((device) => device.role).filter((role): role is string => Boolean(role)))).sort(),
    [devices],
  )
  const locations = useMemo(
    () =>
      Array.from(new Set(devices.map((device) => device.location).filter((location): location is string => Boolean(location)))).sort(),
    [devices],
  )

  async function handleCreateOrUpdateDevice(input: DeviceInput) {
    setFormError(undefined)
    try {
      if (editingDevice) {
        await updateDevice.mutateAsync({ deviceId: editingDevice.id, input })
      } else {
        await createDevice.mutateAsync(input)
      }
      setFormDialogOpen(false)
      setEditingDevice(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDeleteDevice() {
    if (!deletingDevice) return
    setFormError(undefined)
    try {
      await deleteDevice.mutateAsync(deletingDevice.id)
      setDeletingDevice(undefined)
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
          Hardware Inventory
        </Typography>
        <Button
          startIcon={<AddIcon />}
          variant="contained"
          onClick={() => {
            setEditingDevice(undefined)
            setFormError(undefined)
            setFormDialogOpen(true)
          }}
          disabled={!selectedProjectId}
        >
          New device
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

      {devicesPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading devices…</Typography>
        </Stack>
      )}

      {devicesError && <Alert severity="error">Unable to load devices.</Alert>}

      {selectedProjectId && allDevices && (
        <>
          <DeviceFiltersBar roles={roles} locations={locations} filters={filters} onChange={setFilters} />

          <Tabs value={view} onChange={(_event, value) => setView(value)} aria-label="Hardware view">
            <Tab label="Cards" value="cards" />
            <Tab label="Table" value="table" />
          </Tabs>

          <Box role="tabpanel" hidden={view !== 'cards'}>
            {view === 'cards' &&
              (filteredDevices && filteredDevices.length > 0 ? (
                <Grid container spacing={2}>
                  {filteredDevices.map((device) => (
                    <Grid key={device.id} size={{ xs: 12, sm: 6, md: 4 }}>
                      <DeviceCard device={device} onClick={() => setViewingDevice(device)} />
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Typography color="text.secondary">No devices match the current filters.</Typography>
              ))}
          </Box>

          <Box role="tabpanel" hidden={view !== 'table'}>
            {view === 'table' && (
              <DeviceTable devices={filteredDevices ?? EMPTY_DEVICES} onSelectDevice={setViewingDevice} />
            )}
          </Box>
        </>
      )}

      <DeviceFormDialog
        open={formDialogOpen}
        device={editingDevice}
        onClose={() => {
          setFormDialogOpen(false)
          setEditingDevice(undefined)
        }}
        onSubmit={handleCreateOrUpdateDevice}
        onDelete={
          editingDevice
            ? () => {
                setDeletingDevice(editingDevice)
                setFormDialogOpen(false)
              }
            : undefined
        }
        errorMessage={formError}
      />

      <DeviceDetailDialog
        open={Boolean(viewingDevice)}
        device={viewingDevice}
        onClose={() => setViewingDevice(undefined)}
        onEdit={() => {
          if (!viewingDevice) return
          setEditingDevice(viewingDevice)
          setViewingDevice(undefined)
          setFormError(undefined)
          setFormDialogOpen(true)
        }}
      />

      <ConfirmDialog
        open={Boolean(deletingDevice)}
        title="Delete device"
        description={`Delete "${deletingDevice?.name}"? This cannot be undone.`}
        errorMessage={formError}
        confirming={deleteDevice.isPending}
        onCancel={() => setDeletingDevice(undefined)}
        onConfirm={handleDeleteDevice}
      />
    </Stack>
  )
}
