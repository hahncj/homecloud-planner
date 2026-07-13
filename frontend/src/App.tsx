import { Route, Routes } from 'react-router-dom'
import { AppShell } from './layout/AppShell'
import { BackupMatrixPage } from './pages/backup/BackupMatrixPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { DecisionDetailPage } from './pages/decisions/DecisionDetailPage'
import { DecisionsPage } from './pages/decisions/DecisionsPage'
import { HardwarePage } from './pages/hardware/HardwarePage'
import { RoadmapPage } from './pages/roadmap/RoadmapPage'
import { ServiceCatalogPage } from './pages/services/ServiceCatalogPage'
import { ServiceDetailPage } from './pages/services/ServiceDetailPage'
import { SelectedProjectProvider } from './pages/shared/SelectedProjectContext'
import { SettingsPage } from './pages/settings/SettingsPage'
import { ShoppingPage } from './pages/shopping/ShoppingPage'

function App() {
  return (
    <SelectedProjectProvider>
      <AppShell>
        <Routes>
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
        </Routes>
      </AppShell>
    </SelectedProjectProvider>
  )
}

export default App
