import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Grid from '@mui/material/Grid'
import Typography from '@mui/material/Typography'
import type { BudgetSummary } from '../../api/shoppingTypes'

interface BudgetSummaryCardsProps {
  summary: BudgetSummary
}

function formatCurrency(value: string | null): string {
  if (value === null) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value))
}

export function BudgetSummaryCards({ summary }: BudgetSummaryCardsProps) {
  const remainingColor =
    summary.remainingBudget !== null && Number(summary.remainingBudget) < 0 ? 'error.main' : 'text.primary'

  const cards = [
    { label: 'Budget', value: formatCurrency(summary.budget) },
    { label: 'Estimated total', value: formatCurrency(summary.estimatedTotal) },
    { label: 'Actual total', value: formatCurrency(summary.actualTotal) },
    { label: 'Committed spending', value: formatCurrency(summary.committedSpending) },
    { label: 'Remaining budget', value: formatCurrency(summary.remainingBudget), color: remainingColor },
  ]

  return (
    <Grid container spacing={2}>
      {cards.map((card) => (
        <Grid key={card.label} size={{ xs: 12, sm: 6, md: 2.4 }}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="overline" color="text.secondary">
                {card.label}
              </Typography>
              <Typography variant="h6" component="p" sx={{ color: card.color }}>
                {card.value}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  )
}
