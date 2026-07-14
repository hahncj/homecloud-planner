import { z } from 'zod'
import { LIFECYCLE_STATUSES } from '../../api/deviceTypes'

const optionalText = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))

const optionalInteger = z
  .string()
  .trim()
  .transform((value, ctx) => {
    if (value === '') return null
    const parsed = Number(value)
    if (!Number.isInteger(parsed) || parsed < 1 || parsed > 4094) {
      ctx.addIssue({ code: 'custom', message: 'VLAN must be a whole number between 1 and 4094' })
      return z.NEVER
    }
    return parsed
  })

const IPV4_PATTERN = /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
const IPV6_PATTERN = /^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$/
const MAC_PATTERN = /^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$/

const optionalIpAddress = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))
  .refine((value) => value === null || IPV4_PATTERN.test(value) || IPV6_PATTERN.test(value), {
    message: 'Must be a valid IPv4 or IPv6 address',
  })

const optionalMacAddress = z
  .string()
  .trim()
  .transform((value) => (value === '' ? null : value))
  .refine((value) => value === null || MAC_PATTERN.test(value), {
    message: 'Must be a valid MAC address (e.g. AA:BB:CC:DD:EE:FF)',
  })

export const deviceFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(200, 'Must be 200 characters or fewer'),
  manufacturer: optionalText,
  model: optionalText,
  serialNumber: optionalText,
  role: optionalText,
  location: optionalText,
  hostname: optionalText,
  ipAddress: optionalIpAddress,
  macAddress: optionalMacAddress,
  vlan: optionalInteger,
  operatingSystem: optionalText,
  firmwareVersion: optionalText,
  purchaseDate: optionalText,
  warrantyExpiration: optionalText,
  lifecycleStatus: z.enum(LIFECYCLE_STATUSES),
  replacementTarget: optionalText,
  notes: optionalText,
})

export type DeviceFormInput = z.input<typeof deviceFormSchema>
export type DeviceFormOutput = z.output<typeof deviceFormSchema>
