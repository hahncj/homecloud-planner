export type BackupFrequency = 'CONTINUOUS' | 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'MANUAL'

export const BACKUP_FREQUENCIES: BackupFrequency[] = ['CONTINUOUS', 'HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'MANUAL']

export type BackupCoverageState = 'NONE' | 'PARTIAL' | 'FULL'

export const BACKUP_COVERAGE_STATES: BackupCoverageState[] = ['NONE', 'PARTIAL', 'FULL']

export interface BackupPolicy {
  id: string
  projectId: string
  name: string
  dataCategory: string
  primaryLocation: string
  localBackupLocation: string | null
  offsiteBackupLocation: string | null
  encrypted: boolean
  containsSensitiveData: boolean
  frequency: BackupFrequency
  retention: string | null
  recoveryPointObjective: string | null
  recoveryTimeObjective: string | null
  lastVerifiedDate: string | null
  verificationNotes: string | null
  coverageState: BackupCoverageState
  missingLocalBackup: boolean
  missingOffsiteBackup: boolean
  missingEncryptionForSensitiveOffsite: boolean
  verificationOverdue: boolean
  createdAt: string
  updatedAt: string
}

export interface BackupPolicyInput {
  name: string
  dataCategory: string
  primaryLocation: string
  localBackupLocation: string | null
  offsiteBackupLocation: string | null
  encrypted: boolean
  containsSensitiveData: boolean
  frequency: BackupFrequency
  retention: string | null
  recoveryPointObjective: string | null
  recoveryTimeObjective: string | null
  lastVerifiedDate: string | null
  verificationNotes: string | null
}

export interface BackupPolicyFilters {
  coverageState?: BackupCoverageState
  verificationOverdue?: boolean
}
