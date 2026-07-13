import Chip from '@mui/material/Chip'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import type { Phase } from '../../api/roadmapTypes'
import type { PurchaseItem } from '../../api/shoppingTypes'
import { warrantyColor, warrantyLabel, warrantyState } from './warrantyStatus'

interface PurchaseItemTableProps {
  items: PurchaseItem[]
  phaseById: Map<string, Phase>
  onEditItem: (item: PurchaseItem) => void
}

function formatCurrency(value: string | null): string {
  if (value === null) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value))
}

export function PurchaseItemTable({ items, phaseById, onEditItem }: PurchaseItemTableProps) {
  if (items.length === 0) {
    return <Typography color="text.secondary">No purchase items match the current filters.</Typography>
  }

  return (
    <TableContainer sx={{ overflowX: 'auto' }}>
      <Table size="small" aria-label="Purchase items">
        <TableHead>
          <TableRow>
            <TableCell>Product</TableCell>
            <TableCell>Category</TableCell>
            <TableCell>Phase</TableCell>
            <TableCell>Status</TableCell>
            <TableCell align="right">Qty</TableCell>
            <TableCell align="right">Estimated</TableCell>
            <TableCell align="right">Actual</TableCell>
            <TableCell>Warranty</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => {
            const state = warrantyState(item.warrantyExpiration)
            return (
              <TableRow
                key={item.id}
                hover
                onClick={() => onEditItem(item)}
                sx={{ cursor: 'pointer' }}
                tabIndex={0}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') onEditItem(item)
                }}
              >
                <TableCell>{item.productName}</TableCell>
                <TableCell>{item.category}</TableCell>
                <TableCell>{item.phaseId ? (phaseById.get(item.phaseId)?.name ?? '—') : '—'}</TableCell>
                <TableCell>
                  <Chip size="small" label={item.status.replaceAll('_', ' ')} />
                </TableCell>
                <TableCell align="right">{item.quantity}</TableCell>
                <TableCell align="right">{formatCurrency(item.estimatedTotal)}</TableCell>
                <TableCell align="right">{formatCurrency(item.actualTotal)}</TableCell>
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
