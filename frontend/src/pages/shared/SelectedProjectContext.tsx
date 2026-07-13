import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { useProjectsQuery } from '../../api/projects'
import type { Project } from '../../api/roadmapTypes'

interface SelectedProjectContextValue {
  projects: Project[] | undefined
  isPending: boolean
  isError: boolean
  selectedProjectId: string | undefined
  setSelectedProjectId: (projectId: string | undefined) => void
}

const SelectedProjectContext = createContext<SelectedProjectContextValue | undefined>(undefined)

interface SelectedProjectProviderProps {
  children: ReactNode
}

/**
 * Tracks which project is "current" across pages (Roadmap, Shopping, ...).
 * Each page's own local state would reset to the first project on every
 * navigation, silently switching the user away from the project they just
 * picked — this context is the one thing local/server state can't express.
 */
export function SelectedProjectProvider({ children }: SelectedProjectProviderProps) {
  const { data: projects, isPending, isError } = useProjectsQuery()
  const [selectedProjectId, setSelectedProjectId] = useState<string | undefined>(undefined)

  useEffect(() => {
    if (!selectedProjectId && projects && projects.length > 0) {
      setSelectedProjectId(projects[0]?.id)
    }
  }, [projects, selectedProjectId])

  return (
    <SelectedProjectContext.Provider value={{ projects, isPending, isError, selectedProjectId, setSelectedProjectId }}>
      {children}
    </SelectedProjectContext.Provider>
  )
}

export function useSelectedProject(): SelectedProjectContextValue {
  const context = useContext(SelectedProjectContext)
  if (!context) {
    throw new Error('useSelectedProject must be used within a SelectedProjectProvider')
  }
  return context
}
