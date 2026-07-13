import Alert from '@mui/material/Alert'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import CircularProgress from '@mui/material/CircularProgress'
import Grid from '@mui/material/Grid'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import type { ReactNode } from 'react'
import { useDashboardQuery } from '../../api/dashboard'
import { useSelectedProject } from '../shared/SelectedProjectContext'
import { BackupWarningsSummary } from './BackupWarningsSummary'
import { DashboardSummaryCards } from './DashboardSummaryCards'
import { RecommendedActionsList } from './RecommendedActionsList'
import { StatusCountList } from './StatusCountList'
import { TaskSummaryList } from './TaskSummaryList'
import { WarrantyExpirationsList } from './WarrantyExpirationsList'

function SectionCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" component="h3" gutterBottom>
          {title}
        </Typography>
        {children}
      </CardContent>
    </Card>
  )
}

export function DashboardPage() {
  const {
    projects,
    isPending: projectsPending,
    isError: projectsError,
    selectedProjectId,
    setSelectedProjectId,
  } = useSelectedProject()

  const { data: dashboard, isPending: dashboardPending, isError: dashboardError } = useDashboardQuery(selectedProjectId)

  if (projectsPending) {
    return (
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
        <CircularProgress size={20} />
        <Typography>Loading projects…</Typography>
      </Stack>
    )
  }

  if (projectsError) {
    return <Alert severity="error">Unable to load projects.</Alert>
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h2">
        Dashboard
      </Typography>

      {projects && projects.length === 0 && (
        <Alert severity="info">No projects yet. Create one from the Roadmap page to see your dashboard.</Alert>
      )}

      {projects && projects.length > 0 && (
        <TextField
          select
          label="Project"
          value={selectedProjectId ?? ''}
          onChange={(event) => setSelectedProjectId(event.target.value)}
          sx={{ minWidth: 260 }}
        >
          {projects.map((project) => (
            <MenuItem key={project.id} value={project.id}>
              {project.name}
            </MenuItem>
          ))}
        </TextField>
      )}

      {dashboardPending && selectedProjectId && (
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
          <CircularProgress size={20} />
          <Typography>Loading dashboard…</Typography>
        </Stack>
      )}

      {dashboardError && <Alert severity="error">Unable to load the dashboard.</Alert>}

      {dashboard && (
        <>
          <DashboardSummaryCards summary={dashboard} />

          <Grid container spacing={2}>
            <Grid size={{ xs: 12, md: 6 }}>
              <SectionCard title="Next recommended actions">
                <RecommendedActionsList actions={dashboard.nextRecommendedActions} />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <SectionCard title="Backup coverage warnings">
                <BackupWarningsSummary warnings={dashboard.backupCoverageWarnings} />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <SectionCard title="Blocked tasks">
                <TaskSummaryList tasks={dashboard.blockedTasks} emptyMessage="No blocked tasks." dateLabel="target" />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <SectionCard title="Upcoming target dates">
                <TaskSummaryList
                  tasks={dashboard.upcomingTargetDates}
                  emptyMessage="Nothing due in the next 30 days."
                  dateLabel="target"
                />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <SectionCard title="Recently completed">
                <TaskSummaryList
                  tasks={dashboard.recentCompletedTasks}
                  emptyMessage="No tasks completed yet."
                  dateLabel="completed"
                />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <SectionCard title="Upcoming warranty expirations">
                <WarrantyExpirationsList items={dashboard.upcomingWarrantyExpirations} />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 4 }}>
              <SectionCard title="Purchases by status">
                <StatusCountList counts={dashboard.purchaseStatusCounts} emptyMessage="No purchase items yet." />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 4 }}>
              <SectionCard title="Devices by lifecycle">
                <StatusCountList counts={dashboard.deviceLifecycleCounts} emptyMessage="No devices yet." />
              </SectionCard>
            </Grid>

            <Grid size={{ xs: 12, md: 4 }}>
              <SectionCard title="Services by status">
                <StatusCountList counts={dashboard.serviceStatusCounts} emptyMessage="No services yet." />
              </SectionCard>
            </Grid>
          </Grid>
        </>
      )}
    </Stack>
  )
}
