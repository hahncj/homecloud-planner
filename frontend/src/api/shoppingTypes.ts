export type PurchaseStatus = 'IDEA' | 'RESEARCHING' | 'PLANNED' | 'ORDERED' | 'RECEIVED' | 'INSTALLED' | 'CANCELLED'

export const PURCHASE_STATUSES: PurchaseStatus[] = [
  'IDEA',
  'RESEARCHING',
  'PLANNED',
  'ORDERED',
  'RECEIVED',
  'INSTALLED',
  'CANCELLED',
]

export interface PurchaseItem {
  id: string
  projectId: string
  phaseId: string | null
  category: string
  productName: string
  manufacturer: string | null
  model: string | null
  description: string | null
  quantity: number
  estimatedUnitPrice: string | null
  actualUnitPrice: string | null
  estimatedTotal: string | null
  actualTotal: string | null
  vendor: string | null
  purchaseUrl: string | null
  status: PurchaseStatus
  purchaseDate: string | null
  deliveryDate: string | null
  warrantyExpiration: string | null
  receiptReference: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface PurchaseItemInput {
  phaseId: string | null
  category: string
  productName: string
  manufacturer: string | null
  model: string | null
  description: string | null
  quantity: number
  estimatedUnitPrice: number | null
  actualUnitPrice: number | null
  vendor: string | null
  purchaseUrl: string | null
  status: PurchaseStatus
  purchaseDate: string | null
  deliveryDate: string | null
  warrantyExpiration: string | null
  receiptReference: string | null
  notes: string | null
}

export interface PurchaseItemFilters {
  status?: PurchaseStatus
  category?: string
  phaseId?: string
}

export interface CategorySummary {
  category: string
  itemCount: number
  estimatedTotal: string
  actualTotal: string
  committedSpending: string
}

export interface BudgetSummary {
  budget: string | null
  estimatedTotal: string
  actualTotal: string
  committedSpending: string
  remainingBudget: string | null
  categories: CategorySummary[]
}
