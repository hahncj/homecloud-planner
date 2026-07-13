import { Route, Routes } from 'react-router-dom'
import { AppShell } from './layout/AppShell'
import { DashboardPage } from './pages/DashboardPage'
import { HardwarePage } from './pages/hardware/HardwarePage'
import { RoadmapPage } from './pages/roadmap/RoadmapPage'
import { ServiceCatalogPage } from './pages/services/ServiceCatalogPage'
import { ServiceDetailPage } from './pages/services/ServiceDetailPage'
import { SelectedProjectProvider } from './pages/shared/SelectedProjectContext'
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
        </Routes>
      </AppShell>
    </SelectedProjectProvider>
  )
}

export default App
