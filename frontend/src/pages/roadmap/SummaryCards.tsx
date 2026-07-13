import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Grid from '@mui/material/Grid'
import LinearProgress from '@mui/material/LinearProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { Project } from '../../api/roadmapTypes'

interface SummaryCardsProps {
  project: Project
}

function formatCurrency(value: string | null): string {
  if (value === null) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value))
}

export function SummaryCards({ project }: SummaryCardsProps) {
  const { progress } = project

  const cards = [
    { label: 'Status', value: project.status.replaceAll('_', ' ') },
    { label: 'Budget', value: formatCurrency(project.budget) },
    { label: 'Tasks', value: `${progress.completedCount} / ${progress.taskCount} complete` },
    { label: 'Blocked tasks', value: String(progress.blockedCount) },
  ]

  return (
    <Grid container spacing={2}>
      {cards.map((card) => (
        <Grid key={card.label} size={{ xs: 12, sm: 6, md: 3 }}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="overline" color="text.secondary">
                {card.label}
              </Typography>
              <Typography variant="h6" component="p">
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
                {progress.progressPercentage}%
              </Typography>
            </Stack>
            <LinearProgress
              variant="determinate"
              value={progress.progressPercentage}
              aria-label="Overall project progress"
              sx={{ height: 8, borderRadius: 1 }}
            />
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  )
}
