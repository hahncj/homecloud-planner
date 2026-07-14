export type DecisionStatus = 'PROPOSED' | 'ACCEPTED' | 'DEPRECATED' | 'SUPERSEDED' | 'REJECTED'

export const DECISION_STATUSES: DecisionStatus[] = ['PROPOSED', 'ACCEPTED', 'DEPRECATED', 'SUPERSEDED', 'REJECTED']

export interface RelatedEntitySummary {
  id: string
  name: string
}

export interface ArchitectureDecision {
  id: string
  projectId: string
  title: string
  status: DecisionStatus
  context: string | null
  decision: string
  alternativesConsidered: string | null
  consequences: string | null
  decisionDate: string | null
  revisitCriteria: string | null
  relatedDevices: RelatedEntitySummary[]
  relatedServices: RelatedEntitySummary[]
  createdAt: string
  updatedAt: string
}

export interface ArchitectureDecisionInput {
  title: string
  status: DecisionStatus
  context: string | null
  decision: string
  alternativesConsidered: string | null
  consequences: string | null
  decisionDate: string | null
  revisitCriteria: string | null
  relatedDeviceIds: string[]
  relatedServiceIds: string[]
}

export interface ArchitectureDecisionFilters {
  status?: DecisionStatus
}
