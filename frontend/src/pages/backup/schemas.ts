import { z } from 'zod'
import { BACKUP_FREQUENCIES } from '../../api/backupTypes'

const optionalText = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))

export const backupPolicyFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(200, 'Must be 200 characters or fewer'),
  dataCategory: z.string().trim().min(1, 'Data category is required').max(200, 'Must be 200 characters or fewer'),
  primaryLocation: z.string().trim().min(1, 'Primary location is required').max(200, 'Must be 200 characters or fewer'),
  localBackupLocation: optionalText,
  offsiteBackupLocation: optionalText,
  encrypted: z.boolean(),
  containsSensitiveData: z.boolean(),
  frequency: z.enum(BACKUP_FREQUENCIES),
  retention: optionalText,
  recoveryPointObjective: optionalText,
  recoveryTimeObjective: optionalText,
  lastVerifiedDate: optionalText,
  verificationNotes: optionalText,
})

export type BackupPolicyFormInput = z.input<typeof backupPolicyFormSchema>
export type BackupPolicyFormOutput = z.output<typeof backupPolicyFormSchema>
