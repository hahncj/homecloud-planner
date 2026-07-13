import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import type { ReactNode } from 'react'

interface AppShellProps {
  children: ReactNode
}

export function AppShell({ children }: AppShellProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="h1">
            HomeCloud Planner
          </Typography>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ flexGrow: 1, py: 4 }}>
        {children}
      </Container>
    </Box>
  )
}
