# HomeCloud Planner — Frontend

React + TypeScript (strict) + Vite frontend for HomeCloud Planner.

## Stack

- React, TypeScript strict mode, Vite
- Material UI
- React Router
- TanStack Query
- Vitest + React Testing Library

## Development

```bash
npm install
npm run dev      # http://localhost:3000
npm run lint
npm run test      # vitest run
npm run build     # tsc -b && vite build
```

## Configuration

`VITE_API_BASE_URL` is read from the repository root `.env` (see `../.env.example`) and points at the backend API, e.g. `http://localhost:8080/api/v1`.
