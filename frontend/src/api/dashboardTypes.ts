import type { LifecycleStatus } from './deviceTypes'
import type { ProgressSummary } from './roadmapTypes'
import type { ManagedServiceStatus } from './serviceCatalogTypes'
import type { PurchaseStatus } from './shoppingTypes'
import type { TaskPriority, TaskStatus } from './roadmapTypes'

export interface TaskSummary {
  id: string
  title: string
  phaseId: string
  phaseName: string
  status: TaskStatus
  priority: TaskPriority
  targetDate: string | null
  completedDate: string | null
  blocked: boolean
}

export interface PhaseSummary {
  id: string
  name: string
  sequence: number
  progress: ProgressSummary
}

export interface WarrantyExpirationSummary {
  entityType: 'DEVICE' | 'PURCHASE_ITEM'
  id: string
  name: string
  warrantyExpiration: string
}

export interface BackupCoverageWarningCounts {
  missingLocalBackupCount: number
  missingOffsiteBackupCount: number
  missingEncryptionForSensitiveOffsiteCount: number
  verificationOverdueCount: number
}

export interface RecommendedAction {
  category: string
  message: string
}

export interface DashboardSummary {
  projectId: string
  projectName: string
  overallProgress: ProgressSummary
  currentPhase: PhaseSummary | null
  totalBudget: string | null
  estimatedSpending: string
  actualSpending: string
  remainingBudget: string | null
  blockedTaskCount: number
  blockedTasks: TaskSummary[]
  upcomingTargetDates: TaskSummary[]
  recentCompletedTasks: TaskSummary[]
  purchaseStatusCounts: Partial<Record<PurchaseStatus, number>>
  deviceLifecycleCounts: Partial<Record<LifecycleStatus, number>>
  serviceStatusCounts: Partial<Record<ManagedServiceStatus, number>>
  backupCoverageWarnings: BackupCoverageWarningCounts
  upcomingWarrantyExpirations: WarrantyExpirationSummary[]
  nextRecommendedActions: RecommendedAction[]
}
