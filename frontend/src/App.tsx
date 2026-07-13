import { Route, Routes } from 'react-router-dom'
import { AppShell } from './layout/AppShell'
import { DashboardPage } from './pages/DashboardPage'
import { RoadmapPage } from './pages/roadmap/RoadmapPage'

function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/roadmap" element={<RoadmapPage />} />
      </Routes>
    </AppShell>
  )
}

export default App
