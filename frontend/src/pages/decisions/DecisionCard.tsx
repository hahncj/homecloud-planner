import Card from '@mui/material/Card'
import CardActionArea from '@mui/material/CardActionArea'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import Typography from '@mui/material/Typography'
import type { ArchitectureDecision } from '../../api/decisionTypes'
import { decisionStatusColor } from './statusColors'

interface DecisionCardProps {
  decision: ArchitectureDecision
  onClick: () => void
}

export function DecisionCard({ decision, onClick }: DecisionCardProps) {
  return (
    <Card variant="outlined">
      <CardActionArea onClick={onClick} aria-label={`View ${decision.title}`}>
        <CardContent>
          <Typography variant="subtitle1" component="h3">
            {decision.title}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {decision.decisionDate ?? 'No decision date on file'}
          </Typography>
          <Chip size="small" label={decision.status} color={decisionStatusColor(decision.status)} />
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
