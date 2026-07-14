import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const dirname = path.dirname(fileURLToPath(import.meta.url))

function loadRootEnv(): Record<string, string> {
  const envPath = path.resolve(dirname, '../../.env')
  if (!fs.existsSync(envPath)) return {}
  const result: Record<string, string> = {}
  for (const line of fs.readFileSync(envPath, 'utf-8').split('\n')) {
    const trimmed = line.trim()
    if (!trimmed || trimmed.startsWith('#')) continue
    const eq = trimmed.indexOf('=')
    if (eq === -1) continue
    result[trimmed.slice(0, eq)] = trimmed.slice(eq + 1)
  }
  return result
}

// The e2e suite drives a real, already-running stack, so it needs the same
// admin credentials that stack was started with — read from the process
// environment first (CI, or an explicit override) and fall back to the
// workspace-root .env file docker compose itself reads.
const rootEnv = loadRootEnv()

export const ADMIN_USERNAME = process.env.ADMIN_USERNAME ?? rootEnv.ADMIN_USERNAME ?? 'admin'
export const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD ?? rootEnv.ADMIN_PASSWORD ?? 'change-me'
