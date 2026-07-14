import { z } from 'zod'
import { PROJECT_STATUSES, TASK_PRIORITIES, TASK_STATUSES } from '../../api/roadmapTypes'

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

export const projectFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(200, 'Must be 200 characters or fewer'),
  description: optionalText,
  status: z.enum(PROJECT_STATUSES),
  budget: optionalNonNegativeNumber,
  startDate: optionalText,
  targetDate: optionalText,
})

export type ProjectFormInput = z.input<typeof projectFormSchema>
export type ProjectFormOutput = z.output<typeof projectFormSchema>

export const phaseFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(200, 'Must be 200 characters or fewer'),
  description: optionalText,
})

export type PhaseFormInput = z.input<typeof phaseFormSchema>
export type PhaseFormOutput = z.output<typeof phaseFormSchema>

export const taskFormSchema = z.object({
  title: z.string().trim().min(1, 'Title is required').max(200, 'Must be 200 characters or fewer'),
  description: optionalText,
  status: z.enum(TASK_STATUSES),
  priority: z.enum(TASK_PRIORITIES),
  estimatedCost: optionalNonNegativeNumber,
  actualCost: optionalNonNegativeNumber,
  targetDate: optionalText,
  completedDate: optionalText,
  acceptanceCriteria: optionalText,
  notes: optionalText,
})

export type TaskFormInput = z.input<typeof taskFormSchema>
export type TaskFormOutput = z.output<typeof taskFormSchema>
