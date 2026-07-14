import { Outlet, Route, Routes } from 'react-router-dom'
import { AppShell } from './layout/AppShell'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { BackupMatrixPage } from './pages/backup/BackupMatrixPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { DecisionDetailPage } from './pages/decisions/DecisionDetailPage'
import { DecisionsPage } from './pages/decisions/DecisionsPage'
import { HardwarePage } from './pages/hardware/HardwarePage'
import { LoginPage } from './pages/auth/LoginPage'
import { RoadmapPage } from './pages/roadmap/RoadmapPage'
import { ServiceCatalogPage } from './pages/services/ServiceCatalogPage'
import { ServiceDetailPage } from './pages/services/ServiceDetailPage'
import { SelectedProjectProvider } from './pages/shared/SelectedProjectContext'
import { SettingsPage } from './pages/settings/SettingsPage'
import { ShoppingPage } from './pages/shopping/ShoppingPage'

function AuthenticatedLayout() {
  return (
    <SelectedProjectProvider>
      <AppShell>
        <Outlet />
      </AppShell>
    </SelectedProjectProvider>
  )
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AuthenticatedLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/roadmap" element={<RoadmapPage />} />
          <Route path="/shopping" element={<ShoppingPage />} />
          <Route path="/hardware" element={<HardwarePage />} />
          <Route path="/services" element={<ServiceCatalogPage />} />
          <Route path="/services/:serviceId" element={<ServiceDetailPage />} />
          <Route path="/backup" element={<BackupMatrixPage />} />
          <Route path="/decisions" element={<DecisionsPage />} />
          <Route path="/decisions/:decisionId" element={<DecisionDetailPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
