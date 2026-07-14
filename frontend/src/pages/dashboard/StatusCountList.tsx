import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'

interface StatusCountListProps {
  counts: Record<string, number>
  emptyMessage: string
}

export function StatusCountList({ counts, emptyMessage }: StatusCountListProps) {
  const entries = Object.entries(counts)

  if (entries.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        {emptyMessage}
      </Typography>
    )
  }

  return (
    <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
      {entries.map(([status, count]) => (
        <Chip key={status} label={`${status.replaceAll('_', ' ')}: ${count}`} variant="outlined" size="small" />
      ))}
    </Stack>
  )
}
