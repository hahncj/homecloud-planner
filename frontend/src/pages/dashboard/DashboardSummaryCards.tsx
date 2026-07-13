import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Grid from '@mui/material/Grid'
import LinearProgress from '@mui/material/LinearProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { DashboardSummary } from '../../api/dashboardTypes'

interface DashboardSummaryCardsProps {
  summary: DashboardSummary
}

function formatCurrency(value: string | null): string {
  if (value === null) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value))
}

export function DashboardSummaryCards({ summary }: DashboardSummaryCardsProps) {
  const remainingColor =
    summary.remainingBudget !== null && Number(summary.remainingBudget) < 0 ? 'error.main' : 'text.primary'

  const cards = [
    {
      label: 'Current phase',
      value: summary.currentPhase ? summary.currentPhase.name : 'All phases complete',
    },
    { label: 'Blocked tasks', value: String(summary.blockedTaskCount) },
    { label: 'Total budget', value: formatCurrency(summary.totalBudget) },
    { label: 'Estimated spending', value: formatCurrency(summary.estimatedSpending) },
    { label: 'Actual spending', value: formatCurrency(summary.actualSpending) },
    { label: 'Remaining budget', value: formatCurrency(summary.remainingBudget), color: remainingColor },
  ]

  return (
    <Grid container spacing={2}>
      {cards.map((card) => (
        <Grid key={card.label} size={{ xs: 12, sm: 6, md: 4 }}>
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
      <Grid size={12}>
        <Card variant="outlined">
          <CardContent>
            <Stack direction="row" sx={{ justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="overline" color="text.secondary">
                Overall progress
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {summary.overallProgress.progressPercentage}% ({summary.overallProgress.completedCount}/
                {summary.overallProgress.taskCount} tasks)
              </Typography>
            </Stack>
            <LinearProgress
              variant="determinate"
              value={summary.overallProgress.progressPercentage}
              aria-label="Overall project progress"
              sx={{ height: 8, borderRadius: 1 }}
            />
            {summary.currentPhase && (
              <Stack sx={{ mt: 2 }}>
                <Stack direction="row" sx={{ justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="overline" color="text.secondary">
                    Current phase: {summary.currentPhase.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {summary.currentPhase.progress.progressPercentage}%
                  </Typography>
                </Stack>
                <LinearProgress
                  variant="determinate"
                  value={summary.currentPhase.progress.progressPercentage}
                  aria-label={`${summary.currentPhase.name} progress`}
                  color="secondary"
                  sx={{ height: 8, borderRadius: 1 }}
                />
              </Stack>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  )
}
