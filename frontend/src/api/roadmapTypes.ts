export type ProjectStatus = 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED'

export const PROJECT_STATUSES: ProjectStatus[] = [
  'PLANNING',
  'IN_PROGRESS',
  'ON_HOLD',
  'COMPLETED',
  'CANCELLED',
]

export type TaskStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export const TASK_STATUSES: TaskStatus[] = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export const TASK_PRIORITIES: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export interface ProgressSummary {
  taskCount: number
  completedCount: number
  blockedCount: number
  progressPercentage: number
}

export interface Project {
  id: string
  name: string
  description: string | null
  status: ProjectStatus
  budget: string | null
  startDate: string | null
  targetDate: string | null
  createdAt: string
  updatedAt: string
  progress: ProgressSummary
}

export interface ProjectInput {
  name: string
  description: string | null
  status: ProjectStatus
  budget: number | null
  startDate: string | null
  targetDate: string | null
}

export interface Phase {
  id: string
  projectId: string
  name: string
  description: string | null
  sequence: number
  createdAt: string
  updatedAt: string
  progress: ProgressSummary
}

export interface PhaseInput {
  name: string
  description: string | null
}

export interface Task {
  id: string
  phaseId: string
  projectId: string
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  estimatedCost: string | null
  actualCost: string | null
  targetDate: string | null
  completedDate: string | null
  acceptanceCriteria: string | null
  notes: string | null
  blocked: boolean
  dependsOnTaskIds: string[]
  createdAt: string
  updatedAt: string
}

export interface TaskInput {
  title: string
  description: string | null
  status: TaskStatus
  priority: TaskPriority
  estimatedCost: number | null
  actualCost: number | null
  targetDate: string | null
  completedDate: string | null
  acceptanceCriteria: string | null
  notes: string | null
}

export interface TaskFilters {
  phaseId?: string
  status?: TaskStatus
  priority?: TaskPriority
  blocked?: boolean
}

export interface Roadmap {
  project: Project
  phases: Phase[]
  tasks: Task[]
}
