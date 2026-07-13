import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Stack from '@mui/material/Stack'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'

interface AppShellProps {
  children: ReactNode
}

const navLinkStyle = ({ isActive }: { isActive: boolean }) => ({
  color: 'inherit',
  textDecoration: 'none',
  opacity: isActive ? 1 : 0.75,
  fontWeight: isActive ? 600 : 400,
})

export function AppShell({ children }: AppShellProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar sx={{ gap: 3 }}>
          <Typography variant="h6" component="h1">
            HomeCloud Planner
          </Typography>
          <Stack direction="row" spacing={2} component="nav">
            <NavLink to="/" end style={navLinkStyle}>
              Dashboard
            </NavLink>
            <NavLink to="/roadmap" style={navLinkStyle}>
              Roadmap
            </NavLink>
          </Stack>
        </Toolbar>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ flexGrow: 1, py: 4 }}>
        {children}
      </Container>
    </Box>
  )
}
