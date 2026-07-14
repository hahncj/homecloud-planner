import { z } from 'zod'
import { MANAGED_SERVICE_STATUSES, RUNTIME_TYPES, SENSITIVITIES } from '../../api/serviceCatalogTypes'

const optionalText = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))

export const serviceFormSchema = z.object({
  hostDeviceId: z.string(),
  name: z.string().trim().min(1, 'Name is required').max(200, 'Must be 200 characters or fewer'),
  purpose: optionalText,
  description: optionalText,
  status: z.enum(MANAGED_SERVICE_STATUSES),
  runtimeType: z.enum(RUNTIME_TYPES),
  storageLocation: optionalText,
  sensitivity: z.enum(SENSITIVITIES),
  externallyExposed: z.boolean(),
  authenticationMethod: optionalText,
  backupPolicy: optionalText,
  documentationUrl: optionalText,
  repositoryUrl: optionalText,
  notes: optionalText,
})

export type ServiceFormInput = z.input<typeof serviceFormSchema>
export type ServiceFormOutput = z.output<typeof serviceFormSchema>
