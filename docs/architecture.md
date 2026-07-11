# Application Architecture

## Decision summary

HomeCloud Planner is a modular monolith with a separately built React frontend and Spring Boot backend.

## Boundaries

- The backend owns business rules and persistence.
- The frontend owns interaction and presentation.
- PostgreSQL is the system of record.
- Uploaded documents are stored outside PostgreSQL; the database stores metadata and references.
- External integrations are isolated behind interfaces.
- Docker Compose is the primary deployment mechanism.

## Future integrations

Potential adapters include UniFi, Synology, Home Assistant, Prometheus, Docker, Tailscale, and cloud backup providers. These are not part of the MVP.
