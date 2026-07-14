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
import type { Phase } from '../../api/roadmapTypes'
import type { PurchaseItem, PurchaseItemInput } from '../../api/shoppingTypes'
import { PURCHASE_STATUSES } from '../../api/shoppingTypes'
import { purchaseItemFormSchema } from './schemas'
import type { PurchaseItemFormInput, PurchaseItemFormOutput } from './schemas'

interface PurchaseItemFormDialogProps {
  open: boolean
  item?: PurchaseItem
  phases: Phase[]
  onClose: () => void
  onSubmit: (input: PurchaseItemInput) => Promise<unknown>
  onDelete?: () => void
  errorMessage?: string
}

function toDefaultValues(item?: PurchaseItem): PurchaseItemFormInput {
  return {
    phaseId: item?.phaseId ?? '',
    category: item?.category ?? '',
    productName: item?.productName ?? '',
    manufacturer: item?.manufacturer ?? '',
    model: item?.model ?? '',
    description: item?.description ?? '',
    quantity: item ? String(item.quantity) : '1',
    estimatedUnitPrice: item?.estimatedUnitPrice ?? '',
    actualUnitPrice: item?.actualUnitPrice ?? '',
    vendor: item?.vendor ?? '',
    purchaseUrl: item?.purchaseUrl ?? '',
    status: item?.status ?? 'IDEA',
    purchaseDate: item?.purchaseDate ?? '',
    deliveryDate: item?.deliveryDate ?? '',
    warrantyExpiration: item?.warrantyExpiration ?? '',
    receiptReference: item?.receiptReference ?? '',
    notes: item?.notes ?? '',
  }
}

export function PurchaseItemFormDialog({
  open,
  item,
  phases,
  onClose,
  onSubmit,
  onDelete,
  errorMessage,
}: PurchaseItemFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PurchaseItemFormInput, unknown, PurchaseItemFormOutput>({
    resolver: zodResolver(purchaseItemFormSchema),
    values: toDefaultValues(item),
  })

  const submit = handleSubmit(async (values) => {
    await onSubmit({
      phaseId: values.phaseId === '' ? null : values.phaseId,
      category: values.category,
      productName: values.productName,
      manufacturer: values.manufacturer,
      model: values.model,
      description: values.description,
      quantity: values.quantity,
      estimatedUnitPrice: values.estimatedUnitPrice,
      actualUnitPrice: values.actualUnitPrice,
      vendor: values.vendor,
      purchaseUrl: values.purchaseUrl,
      status: values.status,
      purchaseDate: values.purchaseDate,
      deliveryDate: values.deliveryDate,
      warrantyExpiration: values.warrantyExpiration,
      receiptReference: values.receiptReference,
      notes: values.notes,
    })
    reset()
  })

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{item ? 'Edit purchase item' : 'New purchase item'}</DialogTitle>
      <form onSubmit={submit} noValidate>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="category"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Category"
                    required
                    fullWidth
                    autoFocus
                    error={Boolean(errors.category)}
                    helperText={errors.category?.message}
                  />
                )}
              />
              <Controller
                name="productName"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Product name"
                    required
                    fullWidth
                    error={Boolean(errors.productName)}
                    helperText={errors.productName?.message}
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="manufacturer"
                control={control}
                render={({ field }) => <TextField {...field} label="Manufacturer" fullWidth />}
              />
              <Controller
                name="model"
                control={control}
                render={({ field }) => <TextField {...field} label="Model" fullWidth />}
              />
            </Stack>

            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Description" fullWidth multiline minRows={2} />
              )}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="phaseId"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Related phase" fullWidth>
                    <MenuItem value="">None</MenuItem>
                    {phases.map((phase) => (
                      <MenuItem key={phase.id} value={phase.id}>
                        {phase.name}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <Controller
                name="status"
                control={control}
                render={({ field }) => (
                  <TextField {...field} select label="Status" fullWidth>
                    {PURCHASE_STATUSES.map((status) => (
                      <MenuItem key={status} value={status}>
                        {status.replaceAll('_', ' ')}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="quantity"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Quantity"
                    type="number"
                    required
                    fullWidth
                    slotProps={{ htmlInput: { min: 1, step: '1' } }}
                    error={Boolean(errors.quantity)}
                    helperText={errors.quantity?.message}
                  />
                )}
              />
              <Controller
                name="estimatedUnitPrice"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Estimated unit price"
                    type="number"
                    fullWidth
                    slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                    error={Boolean(errors.estimatedUnitPrice)}
                    helperText={errors.estimatedUnitPrice?.message}
                  />
                )}
              />
              <Controller
                name="actualUnitPrice"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Actual unit price"
                    type="number"
                    fullWidth
                    slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
                    error={Boolean(errors.actualUnitPrice)}
                    helperText={errors.actualUnitPrice?.message}
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="vendor"
                control={control}
                render={({ field }) => <TextField {...field} label="Vendor" fullWidth />}
              />
              <Controller
                name="purchaseUrl"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Purchase URL"
                    fullWidth
                    error={Boolean(errors.purchaseUrl)}
                    helperText={errors.purchaseUrl?.message}
                  />
                )}
              />
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Controller
                name="purchaseDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Purchase date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
              <Controller
                name="deliveryDate"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Delivery date"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
              <Controller
                name="warrantyExpiration"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Warranty expiration"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                )}
              />
            </Stack>

            <Controller
              name="receiptReference"
              control={control}
              render={({ field }) => <TextField {...field} label="Receipt reference" fullWidth />}
            />

            <Controller
              name="notes"
              control={control}
              render={({ field }) => <TextField {...field} label="Notes" fullWidth multiline minRows={2} />}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          {item && onDelete && (
            <Button color="error" onClick={onDelete} sx={{ mr: 'auto' }}>
              Delete item
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
