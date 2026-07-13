import Chip from '@mui/material/Chip'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import type { Device } from '../../api/deviceTypes'
import { warrantyColor, warrantyLabel, warrantyState } from '../shared/warrantyStatus'
import { lifecycleColor } from './lifecycleColors'

interface DeviceTableProps {
  devices: Device[]
  onSelectDevice: (device: Device) => void
}

export function DeviceTable({ devices, onSelectDevice }: DeviceTableProps) {
  if (devices.length === 0) {
    return <Typography color="text.secondary">No devices match the current filters.</Typography>
  }

  return (
    <TableContainer sx={{ overflowX: 'auto' }}>
      <Table size="small" aria-label="Devices">
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Role</TableCell>
            <TableCell>Location</TableCell>
            <TableCell>Hostname / IP</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Warranty</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {devices.map((device) => {
            const state = warrantyState(device.warrantyExpiration)
            return (
              <TableRow
                key={device.id}
                hover
                onClick={() => onSelectDevice(device)}
                sx={{ cursor: 'pointer' }}
                tabIndex={0}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') onSelectDevice(device)
                }}
              >
                <TableCell>{device.name}</TableCell>
                <TableCell>{device.role ?? '—'}</TableCell>
                <TableCell>{device.location ?? '—'}</TableCell>
                <TableCell>{[device.hostname, device.ipAddress].filter(Boolean).join(' / ') || '—'}</TableCell>
                <TableCell>
                  <Chip size="small" label={device.lifecycleStatus} color={lifecycleColor(device.lifecycleStatus)} />
                </TableCell>
                <TableCell>
                  {state !== 'none' && (
                    <Chip size="small" label={warrantyLabel(state)} color={warrantyColor(state)} variant="outlined" />
                  )}
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
