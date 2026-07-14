import { zodResolver } from '@hookform/resolvers/zod'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { Controller, useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import type { Location } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { loginFormSchema } from './schemas'
import type { LoginFormInput, LoginFormOutput } from './schemas'

export function LoginPage() {
  const { isAuthenticated, isPending, login, isLoggingIn, loginError } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormInput, unknown, LoginFormOutput>({
    resolver: zodResolver(loginFormSchema),
    defaultValues: { username: '', password: '' },
  })

  if (!isPending && isAuthenticated) {
    const from = (location.state as { from?: Location } | null)?.from
    return <Navigate to={from ? `${from.pathname}${from.search}` : '/'} replace />
  }

  const submit = handleSubmit(async (values) => {
    try {
      await login(values)
      const from = (location.state as { from?: Location } | null)?.from
      navigate(from ? `${from.pathname}${from.search}` : '/', { replace: true })
    } catch {
      // loginError (derived from the mutation's error state) already
      // surfaces the message; nothing further to do here.
    }
  })

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', px: 2 }}>
      <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 400 }}>
        <Typography variant="h5" component="h1" gutterBottom>
          HomeCloud Planner
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Sign in to continue
        </Typography>
        <form onSubmit={submit} noValidate>
          <Stack spacing={2}>
            {loginError && <Alert severity="error">{loginError}</Alert>}

            <Controller
              name="username"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Username"
                  required
                  fullWidth
                  autoFocus
                  autoComplete="username"
                  error={Boolean(errors.username)}
                  helperText={errors.username?.message}
                />
              )}
            />

            <Controller
              name="password"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Password"
                  type="password"
                  required
                  fullWidth
                  autoComplete="current-password"
                  error={Boolean(errors.password)}
                  helperText={errors.password?.message}
                />
              )}
            />

            <Button type="submit" variant="contained" size="large" loading={isLoggingIn}>
              Sign in
            </Button>
          </Stack>
        </form>
      </Paper>
    </Box>
  )
}
