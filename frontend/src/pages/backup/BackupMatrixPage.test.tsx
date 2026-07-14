import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { BackupMatrixPage } from './BackupMatrixPage'
import type { BackupPolicy } from '../../api/backupTypes'
import type { Project } from '../../api/roadmapTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-13T00:00:00Z'

function buildProject(): Project {
  return {
    id: 'project-1',
    name: 'Personal Hybrid Cloud',
    description: null,
    status: 'IN_PROGRESS',
    budget: null,
    startDate: null,
    targetDate: null,
    createdAt: now,
    updatedAt: now,
    progress: { taskCount: 0, completedCount: 0, blockedCount: 0, progressPercentage: 0 },
  }
}

function buildPolicies(): BackupPolicy[] {
  return [
    {
      id: 'policy-1',
      projectId: 'project-1',
      name: 'Photos backup',
      dataCategory: 'Photos',
      primaryLocation: 'NAS volume1',
      localBackupLocation: null,
      offsiteBackupLocation: null,
      encrypted: false,
      containsSensitiveData: false,
      frequency: 'DAILY',
      retention: null,
      recoveryPointObjective: null,
      recoveryTimeObjective: null,
      lastVerifiedDate: null,
      verificationNotes: null,
      coverageState: 'NONE',
      missingLocalBackup: true,
      missingOffsiteBackup: true,
      missingEncryptionForSensitiveOffsite: false,
      verificationOverdue: true,
      createdAt: now,
      updatedAt: now,
    },
    {
      id: 'policy-2',
      projectId: 'project-1',
      name: 'Financial records backup',
      dataCategory: 'Financial records',
      primaryLocation: 'NAS volume1',
      localBackupLocation: 'NAS volume2',
      offsiteBackupLocation: 'Backblaze B2',
      encrypted: false,
      containsSensitiveData: true,
      frequency: 'DAILY',
      retention: null,
      recoveryPointObjective: null,
      recoveryTimeObjective: null,
      lastVerifiedDate: '2026-07-01',
      verificationNotes: null,
      coverageState: 'FULL',
      missingLocalBackup: false,
      missingOffsiteBackup: false,
      missingEncryptionForSensitiveOffsite: true,
      verificationOverdue: false,
      createdAt: now,
      updatedAt: now,
    },
  ]
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }),
  )
}

function renderBackupMatrixPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <BackupMatrixPage />
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('BackupMatrixPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderBackupMatrixPage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading projects/i)
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

    renderBackupMatrixPage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('always shows the RAID/snapshot disclaimer', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.endsWith('/projects')) return jsonResponse([])
        return jsonResponse({}, 404)
      }),
    )

    renderBackupMatrixPage()

    expect(await screen.findByText(/do not count as a backup on their own/i)).toBeInTheDocument()
  })

  it('renders the matrix with coverage and warning indicators', async () => {
    const project = buildProject()
    const policies = buildPolicies()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/backup-policies')) return jsonResponse(policies)
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderBackupMatrixPage()

    expect(await screen.findByText('Photos')).toBeInTheDocument()
    expect(screen.getByText('Financial records')).toBeInTheDocument()
    expect(screen.getByText('NONE')).toBeInTheDocument()
    expect(screen.getByText('FULL')).toBeInTheDocument()
    expect(screen.getByLabelText('Sensitive off-site data is not encrypted')).toBeInTheDocument()
  })
})
