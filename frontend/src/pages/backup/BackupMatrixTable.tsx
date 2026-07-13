import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutlined'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import Chip from '@mui/material/Chip'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import type { BackupPolicy } from '../../api/backupTypes'
import { coverageColor } from './coverageColors'

interface BackupMatrixTableProps {
  policies: BackupPolicy[]
  onSelectPolicy: (policy: BackupPolicy) => void
}

function WarningCell({ warning, okLabel, warningLabel }: { warning: boolean; okLabel: string; warningLabel: string }) {
  return warning ? (
    <Tooltip title={warningLabel}>
      <WarningAmberIcon color="error" fontSize="small" aria-label={warningLabel} />
    </Tooltip>
  ) : (
    <Tooltip title={okLabel}>
      <CheckCircleOutlineIcon color="success" fontSize="small" aria-label={okLabel} />
    </Tooltip>
  )
}

export function BackupMatrixTable({ policies, onSelectPolicy }: BackupMatrixTableProps) {
  if (policies.length === 0) {
    return <Typography color="text.secondary">No backup policies match the current filters.</Typography>
  }

  return (
    <TableContainer sx={{ overflowX: 'auto' }}>
      <Table size="small" aria-label="Backup matrix">
        <TableHead>
          <TableRow>
            <TableCell>Data category</TableCell>
            <TableCell>Coverage</TableCell>
            <TableCell>Local</TableCell>
            <TableCell>Off-site</TableCell>
            <TableCell>Encryption</TableCell>
            <TableCell>Verification</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {policies.map((policy) => (
            <TableRow
              key={policy.id}
              hover
              onClick={() => onSelectPolicy(policy)}
              sx={{ cursor: 'pointer' }}
              tabIndex={0}
              onKeyDown={(event) => {
                if (event.key === 'Enter') onSelectPolicy(policy)
              }}
            >
              <TableCell>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {policy.dataCategory}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {policy.name}
                </Typography>
              </TableCell>
              <TableCell>
                <Chip size="small" label={policy.coverageState} color={coverageColor(policy.coverageState)} />
              </TableCell>
              <TableCell>
                <WarningCell
                  warning={policy.missingLocalBackup}
                  okLabel="Local backup configured"
                  warningLabel="Missing local backup"
                />
              </TableCell>
              <TableCell>
                <WarningCell
                  warning={policy.missingOffsiteBackup}
                  okLabel="Off-site backup configured"
                  warningLabel="Missing off-site backup"
                />
              </TableCell>
              <TableCell>
                {policy.containsSensitiveData ? (
                  <WarningCell
                    warning={policy.missingEncryptionForSensitiveOffsite}
                    okLabel="Encrypted"
                    warningLabel="Sensitive off-site data is not encrypted"
                  />
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    {policy.encrypted ? 'Encrypted' : 'N/A'}
                  </Typography>
                )}
              </TableCell>
              <TableCell>
                {policy.verificationOverdue ? (
                  <Chip size="small" label="Overdue" color="error" variant="outlined" />
                ) : (
                  <Chip size="small" label="Verified" color="success" variant="outlined" />
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
