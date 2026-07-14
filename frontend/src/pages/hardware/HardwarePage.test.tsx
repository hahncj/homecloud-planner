import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { HardwarePage } from './HardwarePage'
import type { Device } from '../../api/deviceTypes'
import type { Project } from '../../api/roadmapTypes'
import { SelectedProjectProvider } from '../shared/SelectedProjectContext'

const now = '2026-07-12T00:00:00Z'

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

function buildDevices(): Device[] {
  return [
    {
      id: 'device-1',
      projectId: 'project-1',
      name: 'Core Switch',
      manufacturer: 'Ubiquiti',
      model: 'USW-24',
      serialNumber: null,
      role: 'Networking',
      location: 'Rack 1',
      hostname: 'switch-01',
      ipAddress: '192.168.1.2',
      macAddress: 'AA:BB:CC:DD:EE:FF',
      vlan: 10,
      operatingSystem: null,
      firmwareVersion: null,
      purchaseDate: null,
      warrantyExpiration: '2020-01-01',
      lifecycleStatus: 'ACTIVE',
      replacementTarget: null,
      notes: null,
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

function renderHardwarePage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <SelectedProjectProvider>
        <HardwarePage />
      </SelectedProjectProvider>
    </QueryClientProvider>,
  )
}

describe('HardwarePage', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows a loading state while projects are being fetched', () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => new Promise(() => {})),
    )

    renderHardwarePage()

    expect(screen.getByRole('status')).toHaveTextContent(/loading projects/i)
  })

  it('shows an error state when projects fail to load', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(new Response(null, { status: 500 }))),
    )

    renderHardwarePage()

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

    renderHardwarePage()

    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument()
  })

  it('renders device cards with lifecycle and warranty indicators', async () => {
    const project = buildProject()
    const devices = buildDevices()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/devices')) return jsonResponse(devices)
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderHardwarePage()

    expect(await screen.findByText('Core Switch')).toBeInTheDocument()
    expect(screen.getByText('ACTIVE')).toBeInTheDocument()
    expect(screen.getByText(/warranty expired/i)).toBeInTheDocument()
  })

  it('opens the device detail view when a card is clicked', async () => {
    const project = buildProject()
    const devices = buildDevices()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/devices')) return jsonResponse(devices)
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderHardwarePage()
    await screen.findByText('Core Switch')

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /view core switch/i }))

    expect(await screen.findByText('switch-01')).toBeInTheDocument()
    expect(screen.getByText('AA:BB:CC:DD:EE:FF')).toBeInTheDocument()
  })

  it('validates required fields when creating a device', async () => {
    const project = buildProject()

    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/devices')) return jsonResponse([])
        if (url.endsWith('/projects')) return jsonResponse([project])
        return jsonResponse({}, 404)
      }),
    )

    renderHardwarePage()
    await screen.findByText(/hardware inventory/i)

    const user = userEvent.setup()
    await user.click(screen.getByRole('button', { name: /new device/i }))
    await user.click(screen.getByRole('button', { name: /^save$/i }))

    expect(await screen.findByText(/name is required/i)).toBeInTheDocument()
  })
})
