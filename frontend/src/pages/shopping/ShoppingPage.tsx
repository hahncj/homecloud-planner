import AddIcon from '@mui/icons-material/Add'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { useMemo, useState } from 'react'
import { useBudgetQuery } from '../../api/budget'
import { ApiError } from '../../api/client'
import { usePhasesQuery } from '../../api/phases'
import {
  useCreatePurchaseItemMutation,
  useDeletePurchaseItemMutation,
  usePurchaseItemsQuery,
  useUpdatePurchaseItemMutation,
} from '../../api/purchaseItems'
import type { Phase } from '../../api/roadmapTypes'
import type { PurchaseItem, PurchaseItemFilters, PurchaseItemInput } from '../../api/shoppingTypes'
import { ConfirmDialog } from '../roadmap/ConfirmDialog'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { BudgetSummaryCards } from './BudgetSummaryCards'
import { CategorySummaryList } from './CategorySummaryList'
import { PurchaseFiltersBar } from './PurchaseFiltersBar'
import { PurchaseItemFormDialog } from './PurchaseItemFormDialog'
import { PurchaseItemTable } from './PurchaseItemTable'

function errorMessageOf(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Something went wrong.'
}

const EMPTY_PHASES: Phase[] = []

type ItemDialogState = { mode: 'create' } | { mode: 'edit'; item: PurchaseItem } | undefined

export function ShoppingPage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()

  const [filters, setFilters] = useState<PurchaseItemFilters>({})
  const [itemDialog, setItemDialog] = useState<ItemDialogState>(undefined)
  const [deletingItem, setDeletingItem] = useState<PurchaseItem | undefined>(undefined)
  const [formError, setFormError] = useState<string | undefined>(undefined)

  const projectId = selectedProjectId ?? ''
  const { data: phases } = usePhasesQuery(selectedProjectId)
  const { data: allItems, isPending: itemsPending, isError: itemsError } = usePurchaseItemsQuery(selectedProjectId)
  const { data: filteredItems } = usePurchaseItemsQuery(selectedProjectId, filters)
  const { data: budget, isPending: budgetPending, isError: budgetError } = useBudgetQuery(selectedProjectId)

  const createItem = useCreatePurchaseItemMutation(projectId)
  const updateItem = useUpdatePurchaseItemMutation()
  const deleteItem = useDeletePurchaseItemMutation()

  const projectPhases = phases ?? EMPTY_PHASES
  const phaseById = useMemo(() => new Map(projectPhases.map((phase) => [phase.id, phase])), [projectPhases])
  const categories = useMemo(
    () => Array.from(new Set((allItems ?? []).map((item) => item.category))).sort(),
    [allItems],
  )

  async function handleCreateOrUpdateItem(input: PurchaseItemInput) {
    setFormError(undefined)
    try {
      if (itemDialog?.mode === 'edit') {
        await updateItem.mutateAsync({ purchaseItemId: itemDialog.item.id, input })
      } else {
        await createItem.mutateAsync(input)
      }
      setItemDialog(undefined)
    } catch (error) {
      setFormError(errorMessageOf(error))
    }
  }

  async function handleDeleteItem() {
    if (!deletingItem) return
    setFormError(undefined)
    try {
      await deleteItem.mutateAsync(deletingItem.id)
      setDeletingItem(undefined)
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
      <Typography variant="h4" component="h2">
        Shopping &amp; Budget
      </Typography>

      {projects && projects.length === 0 && (
        <Alert severity="info">No projects yet. Create one from the Roadmap page to start planning purchases.</Alert>
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

      {budgetPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading budget…</Typography>
        </Stack>
      )}

      {budgetError && <Alert severity="error">Unable to load the budget summary.</Alert>}

      {budget && (
        <>
          <BudgetSummaryCards summary={budget} />

          <Typography variant="h5" component="h3">
            Categories
          </Typography>
          <CategorySummaryList categories={budget.categories} />

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
            <Typography variant="h5" component="h3">
              Shopping list
            </Typography>
            <Button
              startIcon={<AddIcon />}
              variant="contained"
              onClick={() => {
                setFormError(undefined)
                setItemDialog({ mode: 'create' })
              }}
            >
              New purchase item
            </Button>
          </Stack>

          <PurchaseFiltersBar phases={projectPhases} categories={categories} filters={filters} onChange={setFilters} />

          {itemsPending && (
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
              <CircularProgress size={20} />
              <Typography>Loading purchase items…</Typography>
            </Stack>
          )}

          {itemsError && <Alert severity="error">Unable to load purchase items.</Alert>}

          {filteredItems && (
            <PurchaseItemTable
              items={filteredItems}
              phaseById={phaseById}
              onEditItem={(item) => {
                setFormError(undefined)
                setItemDialog({ mode: 'edit', item })
              }}
            />
          )}
        </>
      )}

      {itemDialog && (
        <PurchaseItemFormDialog
          open={Boolean(itemDialog)}
          item={itemDialog.mode === 'edit' ? itemDialog.item : undefined}
          phases={projectPhases}
          onClose={() => setItemDialog(undefined)}
          onSubmit={handleCreateOrUpdateItem}
          onDelete={
            itemDialog.mode === 'edit'
              ? () => {
                  setDeletingItem(itemDialog.item)
                  setItemDialog(undefined)
                }
              : undefined
          }
          errorMessage={formError}
        />
      )}

      <ConfirmDialog
        open={Boolean(deletingItem)}
        title="Delete purchase item"
        description={`Delete "${deletingItem?.productName}"? This cannot be undone.`}
        errorMessage={formError}
        confirming={deleteItem.isPending}
        onCancel={() => setDeletingItem(undefined)}
        onConfirm={handleDeleteItem}
      />
    </Stack>
  )
}
