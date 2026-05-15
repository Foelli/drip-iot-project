---
title: Code + Architecture Source of Truth
---

# Code + Architecture Source of Truth

This document is the implementation contract for maintainers (and coding agents) fulfilling GitHub issues in this repo.

Primary product intent lives in:

- `docs/README.md`
- `docs/Requirements.md`

If code disagrees with those docs, open/fulfill an issue to resolve the mismatch (do not silently drift).

## Repo map (current)

- `ranelle-backend/`: Ktor server + Postgres persistence (Exposed)
- `faron-frontend/`: Vue 3 (Vite) dashboard frontend
- `docs/`: project intent and requirements
- Pico firmware: lives outside this repo today, but is a first-class component of the system (HTTP client talking to the backend)

## System overview

The intended deployed system is:

- Raspberry Pi Pico (or RP2040 board) reads sensors and controls actuators.
- Pico sends readings and receives commands over HTTP to/from the Ktor backend (typically running on a Raspberry Pi).
- Vue dashboard talks directly to the backend over HTTP on the same LAN.
- Production runs on a DIY homeserver using Docker containers for frontend, backend, and Postgres.
- The public dashboard entrypoint is Cloudflare Tunnel in front of the frontend.

## Architecture rules (backend)

The backend is structured as a simple layered architecture:

- `plant/domain/`: pure domain types and interfaces
  - Entities: `plant.domain.entity.*` (Kotlin data classes, serializable when returned via API)
  - Repository interfaces: `plant.domain.repository.*` (no persistence details)
  - Use cases: `plant.domain.usecase.*` (one responsibility, call repository)
- `plant/data/`: persistence implementation
  - Database setup: `plant.data.database.*` (currently `DatabaseFactory` + Exposed table)
  - Repository implementation: `plant.data.repository.*` (Exposed/SQL, transactions)
- `web/`: HTTP boundary
  - DTOs: `web.dto.*` for request payloads
  - Routing: `web.routing.*` for endpoints and HTTP semantics
  - App wiring: `web.Application.module` composes dependencies

### Backend layering constraints

- `web/*` may depend on `domain/*` and call `usecase/*`.
- `domain/*` must not depend on `web/*` or `data/*`.
- `data/*` may depend on `domain/*` but not on `web/*`.
- Keep HTTP concerns out of domain (no `ApplicationCall`, no status codes in use cases).

### Adding or changing a backend feature

Prefer this sequence:

1. Add/adjust a domain type or repository method (in `plant/domain/`).
2. Implement persistence in `plant/data/` (Exposed + transactions).
3. Add/adjust a use case in `plant/domain/usecase/` (thin orchestration).
4. Expose via HTTP in `web/routing/` using DTOs in `web/dto/`.
5. Wire dependencies in `web/Application.module`.

### API contract rules

- All new HTTP APIs are versioned under `/api/v1`.
- Request payloads use DTOs (`web.dto.*`). Do not `receive<Plant>()` directly.
- Return domain entities from routes only when they are already safe to expose.
- Use explicit HTTP status codes (`201` for create, `204` for successful update/delete without body, `404` on missing resource, `400` on invalid path params).
- Keep immutable fields immutable end-to-end (example: `apiId` is set on create and not updateable).
- Keep it simple: no OpenAPI/Swagger requirement at this stage.
- Auth: none for now. If/when auth is introduced, it must be called out explicitly in issues and documented in `docs/README.md`.

### Versioning policy

- New endpoints: must live under `/api/v1/...`.
- Existing non-versioned endpoints may remain temporarily.
- Migrating an existing endpoint into `/api/v1` requires a dedicated issue that defines the compatibility plan (e.g. keep both paths for a time, or break intentionally).

### Persistence rules

- Postgres is the intended long-term database.
- Startup auto-create (current `SchemaUtils.createMissingTablesAndColumns`) is acceptable for now.
- Any change that can cause data loss or table rebuild must be explicitly called out in the issue and PR description.

## Planned domain expansion: sensor readings

The next major backend domain module is sensor readings. When implementing it, follow the same layering pattern:

- `sensor/domain/`: entities (e.g. `Reading`, `Device`), repository interface, use cases
- `sensor/data/`: Exposed tables and repository implementation
- `web/`: DTOs + routes under `/api/v1`

Do not pack sensor-reading persistence and web DTOs into the existing `plant/*` packages.

## Architecture rules (frontend)

Frontend is currently minimal (`Vue 3 + Vite`) and should stay conventional:

- App entry: `faron-frontend/src/main.js`
- Root component: `faron-frontend/src/App.vue`
- Reusable components: `faron-frontend/src/components/`
- Static assets and CSS: `faron-frontend/src/assets/`

When adding real UI:

- Prefer a small number of clear views over many tiny components.
- If you introduce routing/state management, keep it standard (Vue Router, Pinia) and keep API calls in a dedicated module (do not scatter fetch logic across components).
- Frontend talks directly to backend (no BFF layer). Keep API base URL configuration explicit (env or a single config module), not duplicated across components.

## Architecture rules (Pico firmware)

Firmware is treated as a separate codebase, but its contract with this repo is defined here:

- Communication protocol: HTTP
- Payload format: JSON unless a specific issue defines otherwise
- Versioning: firmware should call `/api/v1` endpoints only

When adding endpoints intended for Pico use, the backend issue must define:

- endpoint(s), method(s), request/response JSON shape
- timing/throughput expectations (how often readings are posted)
- failure behavior (retry strategy, status codes treated as retryable)

### Device data flow (intended)

- Readings: Pico pushes sensor readings to the backend via HTTP `POST`.
  - Rationale: the Pico is the source of new measurements; push avoids running an HTTP server on the Pico and avoids backend polling complexity.
- Parameters/config: Pico pulls measurement and watering parameters from the backend via HTTP `GET`.
  - The backend is the source of truth for parameters, updated via the Vue dashboard UI.

## Build and run (current)

Backend (from `ranelle-backend/`):

- Run: `./gradlew run`
- Test: `./gradlew test`

Frontend (from `faron-frontend/`):

- Install: `npm install`
- Dev: `npm run dev`
- Build: `npm run build`

## Deployment architecture (target)

The deployment target is a homeserver running the full stack with Docker:

- `frontend`: serves the built Vue dashboard.
- `backend`: runs the Ktor API.
- `postgres`: stores all persistent backend state.
- `cloudflared` (optional container): exposes the frontend through Cloudflare Tunnel.

Rules:

- Images are built from `main` and published with versioned tags.
- Homeserver pulls released image tags and updates the running containers.
- Postgres data must live in a persistent Docker volume.
- Secrets must be injected via env vars or Docker secrets, not committed files.
- Backend should stay private to the Docker network unless a ticket explicitly exposes it.
- Frontend should use a configured API base URL; do not hard-code local dev ports in production code.

## Definition of done (for issues)

Unless the issue explicitly says otherwise, a PR is "done" when:

- It matches intent in `docs/README.md` and does not violate `docs/Requirements.md`.
- It preserves layering constraints (domain/data/web separation).
- It includes tight acceptance criteria coverage:
  - backend: routes behave as specified and use cases are covered by tests
  - frontend: UI changes are reachable and don’t break build
- It does not introduce new global rewrites/refactors unrelated to the issue.
- It updates docs when the behavior/contract changes.

### What tests make sense here

Backend testing should bias toward fast, stable checks:

- Use case unit tests: required when a use case has logic beyond "call repository".
  - Use a fake/in-memory repository implementation to avoid DB dependencies.
  - Cover edge cases: not found, validation, immutability (e.g. `apiId` not changeable).
- Route tests: add when introducing or changing HTTP semantics.
  - Use Ktor test host to verify status codes and JSON shape.
  - Do not require a running Postgres instance unless the issue is specifically about DB integration.

## Agent-specific guidance

If you are using an agent to implement an issue:

- Start by locating the closest existing file matching the needed change (do not invent new structure first).
- Keep changes localized to the relevant subtree (`ranelle-backend/` vs `faron-frontend/`).
- Prefer incremental, reviewable commits (even if you do not actually commit in the tool).
- When uncertain about a contract (API shape, naming, requirements mapping), add an "Open question" comment to the issue/PR rather than guessing.
