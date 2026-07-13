import DownloadIcon from '@mui/icons-material/Download'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { useState } from 'react'
import { useHealthQuery } from '../../api/health'
import { downloadJsonExport, downloadMarkdownExport } from '../../api/export'

function errorMessageOf(error: unknown): string {
  return error instanceof Error ? error.message : 'Something went wrong.'
}

export function SettingsPage() {
  const { data, isPending, isError, error } = useHealthQuery()
  const [exportError, setExportError] = useState<string | undefined>(undefined)
  const [downloading, setDownloading] = useState<'json' | 'markdown' | undefined>(undefined)

  async function handleDownload(format: 'json' | 'markdown') {
    setExportError(undefined)
    setDownloading(format)
    try {
      if (format === 'json') {
        await downloadJsonExport()
      } else {
        await downloadMarkdownExport()
      }
    } catch (err) {
      setExportError(errorMessageOf(err))
    } finally {
      setDownloading(undefined)
    }
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h2">
        Settings
      </Typography>

      <Card variant="outlined">
        <CardContent>
          <Typography variant="h6" component="h3" gutterBottom>
            Backend status
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

      <Card variant="outlined">
        <CardContent>
          <Typography variant="h6" component="h3" gutterBottom>
            Export
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Download every project's data — phases, tasks and dependencies, purchases, devices, services and their
            dependencies, backup policies, and architecture decisions. Exports never include credentials or
            authentication data.
          </Typography>

          {exportError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {exportError}
            </Alert>
          )}

          <Stack direction="row" spacing={2}>
            <Button
              variant="contained"
              startIcon={<DownloadIcon />}
              loading={downloading === 'json'}
              onClick={() => handleDownload('json')}
            >
              Download JSON
            </Button>
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              loading={downloading === 'markdown'}
              onClick={() => handleDownload('markdown')}
            >
              Download Markdown
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  )
}
