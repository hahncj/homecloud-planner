import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Divider from '@mui/material/Divider'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import type { ArchitectureDecisionInput } from '../../api/decisionTypes'
import { useDecisionQuery, useDeleteDecisionMutation, useUpdateDecisionMutation } from '../../api/decisions'
import { useDevicesQuery } from '../../api/devices'
import { useServicesQuery } from '../../api/services'
import { ConfirmDialog } from '../roadmap/ConfirmDialog'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { DecisionFormDialog } from './DecisionFormDialog'
import { decisionStatusColor } from './statusColors'

function errorMessageOf(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong.'
}

function Section({ title, content }: { title: string; content: string | null }) {
  if (!content) return null
  return (
    <Stack spacing={0.5}>
      <Typography variant="overline" color="text.secondary">
        {title}
      </Typography>
      <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
        {content}
      </Typography>
    </Stack>
  )
}

export function DecisionDetailPage() {
  const { decisionId } = useParams<{ decisionId: string }>()
  const navigate = useNavigate()
  const { setSelectedProjectId } = useSelectedProject()

  const { data: decision, isPending, isError } = useDecisionQuery(decisionId)
  const { data: devices } = useDevicesQuery(decision?.projectId)
  const { data: services } = useServicesQuery(decision?.projectId)

  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const updateDecision = useUpdateDecisionMutation()
  const deleteDecision = useDeleteDecisionMutation()

  useEffect(() => {
    if (decision) {
      setSelectedProjectId(decision.projectId)
    }
  }, [decision, setSelectedProjectId])

  async function handleUpdate(input: ArchitectureDecisionInput) {
    if (!decisionId) return
    setFormError(undefined)
    try {
      await updateDecision.mutateAsync({ decisionId, input })
      setEditDialogOpen(false)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDelete() {
    if (!decisionId) return
    setFormError(undefined)
    try {
      await deleteDecision.mutateAsync(decisionId)
      navigate('/decisions')
    } catch (error) {
      setFormError(errorMessageOf(error))
      setDeleteDialogOpen(false)
    }
  }

  if (isPending) {
    return (
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
        <CircularProgress size={20} />
        <Typography>Loading decision…</Typography>
      </Stack>
    )
  }

  if (isError || !decision) {
    return <Alert severity="error">Unable to load this decision.</Alert>
  }

  return (
    <Stack spacing={3}>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/decisions')} sx={{ alignSelf: 'flex-start' }}>
        Back to decisions
      </Button>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
        <Typography variant="h4" component="h2">
          {decision.title}
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

      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <Chip label={decision.status} color={decisionStatusColor(decision.status)} />
        {decision.decisionDate && (
          <Typography variant="body2" color="text.secondary">
            Decided {decision.decisionDate}
          </Typography>
        )}
      </Stack>

      <Divider />

      <Section title="Context" content={decision.context} />
      <Section title="Decision" content={decision.decision} />
      <Section title="Alternatives considered" content={decision.alternativesConsidered} />
      <Section title="Consequences" content={decision.consequences} />
      <Section title="Revisit criteria" content={decision.revisitCriteria} />

      {(decision.relatedDevices.length > 0 || decision.relatedServices.length > 0) && (
        <>
          <Divider />
          {decision.relatedDevices.length > 0 && (
            <Stack spacing={0.5}>
              <Typography variant="overline" color="text.secondary">
                Related devices
              </Typography>
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                {decision.relatedDevices.map((device) => (
                  <Chip key={device.id} label={device.name} />
                ))}
              </Stack>
            </Stack>
          )}
          {decision.relatedServices.length > 0 && (
            <Stack spacing={0.5}>
              <Typography variant="overline" color="text.secondary">
                Related services
              </Typography>
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                {decision.relatedServices.map((service) => (
                  <Chip key={service.id} label={service.name} />
                ))}
              </Stack>
            </Stack>
          )}
        </>
      )}

      <DecisionFormDialog
        open={editDialogOpen}
        decision={decision}
        devices={devices ?? []}
        services={services ?? []}
        onClose={() => setEditDialogOpen(false)}
        onSubmit={handleUpdate}
        errorMessage={formError}
      />

      <ConfirmDialog
        open={deleteDialogOpen}
        title="Delete decision"
        description={`Delete "${decision.title}"? This cannot be undone.`}
        errorMessage={formError}
        confirming={deleteDecision.isPending}
        onCancel={() => setDeleteDialogOpen(false)}
        onConfirm={handleDelete}
      />
    </Stack>
  )
}
