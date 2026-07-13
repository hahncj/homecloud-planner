import { z } from 'zod'
import { DECISION_STATUSES } from '../../api/decisionTypes'

const optionalText = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))

export const decisionFormSchema = z.object({
  title: z.string().trim().min(1, 'Title is required').max(200, 'Must be 200 characters or fewer'),
  status: z.enum(DECISION_STATUSES),
  context: optionalText,
  decision: z.string().trim().min(1, 'Decision is required'),
  alternativesConsidered: optionalText,
  consequences: optionalText,
  decisionDate: optionalText,
  revisitCriteria: optionalText,
  relatedDeviceIds: z.array(z.string()),
  relatedServiceIds: z.array(z.string()),
})

export type DecisionFormInput = z.input<typeof decisionFormSchema>
export type DecisionFormOutput = z.output<typeof decisionFormSchema>
