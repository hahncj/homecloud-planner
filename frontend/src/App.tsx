import { Route, Routes } from 'react-router-dom'
import { AppShell } from './layout/AppShell'
import { DashboardPage } from './pages/DashboardPage'
import { RoadmapPage } from './pages/roadmap/RoadmapPage'
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
        </Routes>
      </AppShell>
    </SelectedProjectProvider>
  )
}

export default App
