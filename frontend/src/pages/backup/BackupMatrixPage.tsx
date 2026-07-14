import AddIcon from '@mui/icons-material/Add'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useState } from 'react'
import {
  useBackupPoliciesQuery,
  useCreateBackupPolicyMutation,
  useDeleteBackupPolicyMutation,
  useUpdateBackupPolicyMutation,
} from '../../api/backupPolicies'
import type { BackupPolicy, BackupPolicyFilters, BackupPolicyInput } from '../../api/backupTypes'
import { ApiError } from '../../api/client'
import { ConfirmDialog } from '../roadmap/ConfirmDialog'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { BackupFiltersBar } from './BackupFiltersBar'
import { BackupMatrixTable } from './BackupMatrixTable'
import { BackupPolicyFormDialog } from './BackupPolicyFormDialog'

function errorMessageOf(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong.'
}

type PolicyDialogState = { mode: 'create' } | { mode: 'edit'; policy: BackupPolicy } | undefined

export function BackupMatrixPage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()

  const [filters, setFilters] = useState<BackupPolicyFilters>({})
  const [policyDialog, setPolicyDialog] = useState<PolicyDialogState>(undefined)
  const [deletingPolicy, setDeletingPolicy] = useState<BackupPolicy | undefined>(undefined)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const projectId = selectedProjectId ?? ''
  const { data: policies, isPending: policiesPending, isError: policiesError } = useBackupPoliciesQuery(
    selectedProjectId,
    filters,
  )

  const createPolicy = useCreateBackupPolicyMutation(projectId)
  const updatePolicy = useUpdateBackupPolicyMutation()
  const deletePolicy = useDeleteBackupPolicyMutation()

  async function handleCreateOrUpdatePolicy(input: BackupPolicyInput) {
    setFormError(undefined)
    try {
      if (policyDialog?.mode === 'edit') {
        await updatePolicy.mutateAsync({ backupPolicyId: policyDialog.policy.id, input })
      } else {
        await createPolicy.mutateAsync(input)
      }
      setPolicyDialog(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDeletePolicy() {
    if (!deletingPolicy) return
    setFormError(undefined)
    try {
      await deletePolicy.mutateAsync(deletingPolicy.id)
      setDeletingPolicy(undefined)
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
          Backup Matrix
        </Typography>
        <Button
          startIcon={<AddIcon />}
          variant="contained"
          onClick={() => {
            setFormError(undefined)
            setPolicyDialog({ mode: 'create' })
          }}
          disabled={!selectedProjectId}
        >
          New backup policy
        </Button>
      </Stack>

      <Alert severity="info">
        RAID and filesystem snapshots protect against drive failure, not against deletion, ransomware, fire, or
        theft. They do not count as a backup on their own — a real local or off-site backup location is what
        matters here.
      </Alert>

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

      {policiesPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading backup policies…</Typography>
        </Stack>
      )}

      {policiesError && <Alert severity="error">Unable to load backup policies.</Alert>}

      {selectedProjectId && policies && (
        <>
          <BackupFiltersBar filters={filters} onChange={setFilters} />
          <BackupMatrixTable
            policies={policies}
            onSelectPolicy={(policy) => {
              setFormError(undefined)
              setPolicyDialog({ mode: 'edit', policy })
            }}
          />
        </>
      )}

      <BackupPolicyFormDialog
        open={Boolean(policyDialog)}
        policy={policyDialog?.mode === 'edit' ? policyDialog.policy : undefined}
        onClose={() => setPolicyDialog(undefined)}
        onSubmit={handleCreateOrUpdatePolicy}
        onDelete={
          policyDialog?.mode === 'edit'
            ? () => {
                setDeletingPolicy(policyDialog.policy)
                setPolicyDialog(undefined)
              }
            : undefined
        }
        errorMessage={formError}
      />

      <ConfirmDialog
        open={Boolean(deletingPolicy)}
        title="Delete backup policy"
        description={`Delete "${deletingPolicy?.name}"? This cannot be undone.`}
        errorMessage={formError}
        confirming={deletePolicy.isPending}
        onCancel={() => setDeletingPolicy(undefined)}
        onConfirm={handleDeletePolicy}
      />
    </Stack>
  )
}
