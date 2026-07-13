import Alert from '@mui/material/Alert'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { useHealthQuery } from '../api/health'

export function DashboardPage() {
  const { data, isPending, isError, error } = useHealthQuery()

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h2">
        Dashboard
      </Typography>

      <Card variant="outlined">
        <CardContent>
          <Typography variant="h6" component="h3" gutterBottom>
            Backend Status
          </Typography>

          {isPending && (
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }} role="status">
              <CircularProgress size={20} />
              <Typography>Checking backend health…</Typography>
            </Stack>
          )}

          {isError && (
            <Alert severity="error">
              Unable to reach the backend: {error instanceof Error ? error.message : 'Unknown error'}
            </Alert>
          )}

          {!isPending && !isError && data && (
            <Alert severity="success">
              Backend is {data.status} as of {new Date(data.timestamp).toLocaleString()}
            </Alert>
          )}
        </CardContent>
      </Card>
    </Stack>
  )
}
