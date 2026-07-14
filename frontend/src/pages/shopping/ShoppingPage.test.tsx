import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ShoppingPage } from './ShoppingPage'
import type { Project } from '../../api/roadmapTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'
import type { BudgetSummary, PurchaseItem } from '../../api/shoppingTypes'

const now = '2026-07-12T00:00:00Z'

function buildProject(): Project {
  return {
    id: 'project-1',
    name: 'Personal Hybrid Cloud',
    description: null,
    status: 'IN_PROGRESS',
    budget: '1000.00',
    startDate: null,
    targetDate: null,
    createdAt: now,
    updatedAt: now,
    progress: { taskCount: 0, completedCount: 0, blockedCount: 0, progressPercentage: 0 },
  }
}

function buildItems(): PurchaseItem[] {
  return [
    {
      id: 'item-1',
      projectId: 'project-1',
      phaseId: null,
      category: 'Networking',
      productName: 'Managed switch',
      manufacturer: null,
      model: null,
      description: null,
      quantity: 1,
      estimatedUnitPrice: '100.00',
      actualUnitPrice: '90.00',
      estimatedTotal: '100.00',
      actualTotal: '90.00',
      vendor: null,
      purchaseUrl: null,
      status: 'RECEIVED',
      purchaseDate: null,
      deliveryDate: null,
      warrantyExpiration: '2020-01-01',
      receiptReference: null,
      notes: null,
      createdAt: now,
      updatedAt: now,
    },
    {
      id: 'item-2',
      projectId: 'project-1',
      phaseId: null,
      category: 'Storage',
      productName: 'NAS drive',
      manufacturer: null,
      model: null,
      description: null,
      quantity: 2,
      estimatedUnitPrice: '150.00',
      actualUnitPrice: null,
      estimatedTotal: '300.00',
      actualTotal: null,
      vendor: null,
      purchaseUrl: null,
      status: 'PLANNED',
      purchaseDate: null,
      deliveryDate: null,
      warrantyExpiration: null,
      receiptReference: null,
      notes: null,
      createdAt: now,
      updatedAt: now,
    },
  ]
}

function buildBudget(): BudgetSummary {
  return {
    budget: '1000.00',
    estimatedTotal: '400.00',
    actualTotal: '90.00',
    committedSpending: '390.00',
    remainingBudget: '610.00',
    categories: [
      { category: 'Networking', itemCount: 1, estimatedTotal: '100.00', actualTotal: '90.00', committedSpending: '90.00' },
      { category: 'Storage', itemCount: 1, estimatedTotal: '300.00', actualTotal: '0.00', committedSpending: '300.00' },
    ],
  }
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderShoppingPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <ShoppingPage />
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('ShoppingPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderShoppingPage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading projects/i)
  })

  it('shows an error state when projects fail to load', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(new Response(null, { status: 500 }))),
    )

    renderShoppingPage()

    expect(await screen.findByText(/unable to load projects/i)).toBeInTheDocument()
  })

  it('shows an empty state when there are no projects', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.endsWith('/projects')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderShoppingPage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('renders budget summary, category totals, and warranty indicators', async () => {
    const project = buildProject()
    const items = buildItems()
    const budget = buildBudget()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/budget')) return jsonResponse(budget)
        if (url.includes('/purchase-items')) return jsonResponse(items)
        if (url.includes('/phases')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderShoppingPage()

    expect(await screen.findByText('Managed switch')).toBeInTheDocument()
    expect(screen.getByText('NAS drive')).toBeInTheDocument()
    expect(screen.getByText(/warranty expired/i)).toBeInTheDocument()

    const categoryTable = screen.getByRole('table', { name: /category summaries/i })
    expect(within(categoryTable).getByText('Networking')).toBeInTheDocument()
    expect(within(categoryTable).getByText('Storage')).toBeInTheDocument()
  })

  it('validates required fields when creating a purchase item', async () => {
    const project = buildProject()
    const budget = buildBudget()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/budget')) return jsonResponse(budget)
        if (url.includes('/purchase-items')) return jsonResponse([])
        if (url.includes('/phases')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderShoppingPage()
    await screen.findByText(/shopping list/i)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /new purchase item/i }))
    await user.click(screen.getByRole('button', { name: /^save$/i }))

    expect(await screen.findByText(/category is required/i)).toBeInTheDocument()
    expect(screen.getByText(/product name is required/i)).toBeInTheDocument()
  })
})
