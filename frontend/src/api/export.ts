import { API_BASE_URL, ApiError } from './client'

async function downloadFrom(path: string, fallbackFilename: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' })
  if (!response.ok) {
    throw new ApiError(`Export request to ${path} failed with status ${response.status}`, response.status)
  }

  const disposition = response.headers.get('content-disposition')
  const filenameMatch = disposition?.match(/filename="?([^"]+)"?/)
  const filename = filenameMatch?.[1] ?? fallbackFilename

  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

export function downloadJsonExport(): Promise<void> {
  return downloadFrom('/export/json', 'homecloud-planner-export.json')
}

export function downloadMarkdownExport(): Promise<void> {
  return downloadFrom('/export/markdown', 'homecloud-planner-export.md')
}
