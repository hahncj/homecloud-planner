import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import type { CategorySummary } from '../../api/shoppingTypes'

interface CategorySummaryListProps {
  categories: CategorySummary[]
}

function formatCurrency(value: string): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value))
}

export function CategorySummaryList({ categories }: CategorySummaryListProps) {
  if (categories.length === 0) {
    return (
      <Typography color="text.secondary">No non-cancelled purchase items yet.</Typography>
    )
  }

  return (
    <TableContainer sx={{ overflowX: 'auto' }}>
      <Table size="small" aria-label="Category summaries">
        <TableHead>
          <TableRow>
            <TableCell>Category</TableCell>
            <TableCell align="right">Items</TableCell>
            <TableCell align="right">Estimated</TableCell>
            <TableCell align="right">Actual</TableCell>
            <TableCell align="right">Committed</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {categories.map((category) => (
            <TableRow key={category.category}>
              <TableCell>{category.category}</TableCell>
              <TableCell align="right">{category.itemCount}</TableCell>
              <TableCell align="right">{formatCurrency(category.estimatedTotal)}</TableCell>
              <TableCell align="right">{formatCurrency(category.actualTotal)}</TableCell>
              <TableCell align="right">{formatCurrency(category.committedSpending)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
