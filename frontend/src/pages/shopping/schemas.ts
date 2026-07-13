import { z } from 'zod'
import { PURCHASE_STATUSES } from '../../api/shoppingTypes'

const optionalText = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))

const optionalNonNegativeNumber = z
  .string()
  .trim()
  .transform((value, ctx) => {
    if (value === '') return null
    const parsed = Number(value)
    if (Number.isNaN(parsed)) {
      ctx.addIssue({ code: 'custom', message: 'Must be a number' })
      return z.NEVER
    }
    if (parsed < 0) {
      ctx.addIssue({ code: 'custom', message: 'Must not be negative' })
      return z.NEVER
    }
    return parsed
  })

export const purchaseItemFormSchema = z.object({
  phaseId: z.string(),
  category: z.string().trim().min(1, 'Category is required').max(100, 'Must be 100 characters or fewer'),
  productName: z.string().trim().min(1, 'Product name is required').max(200, 'Must be 200 characters or fewer'),
  manufacturer: optionalText,
  model: optionalText,
  description: optionalText,
  quantity: z
    .string()
    .trim()
    .transform((value, ctx) => {
      const parsed = Number(value)
      if (value === '' || Number.isNaN(parsed) || !Number.isInteger(parsed) || parsed < 1) {
        ctx.addIssue({ code: 'custom', message: 'Quantity must be a whole number of at least 1' })
        return z.NEVER
      }
      return parsed
    }),
  estimatedUnitPrice: optionalNonNegativeNumber,
  actualUnitPrice: optionalNonNegativeNumber,
  vendor: optionalText,
  purchaseUrl: optionalText,
  status: z.enum(PURCHASE_STATUSES),
  purchaseDate: optionalText,
  deliveryDate: optionalText,
  warrantyExpiration: optionalText,
  receiptReference: optionalText,
  notes: optionalText,
})

export type PurchaseItemFormInput = z.input<typeof purchaseItemFormSchema>
export type PurchaseItemFormOutput = z.output<typeof purchaseItemFormSchema>
