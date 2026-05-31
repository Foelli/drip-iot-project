# Project Spec (Draft)

This is a single living spec for the whole system (frontend, backend, firmware).

Sources of intent:

- `docs/README.md`
- `docs/Requirements.md`
- `docs/ARCHITECTURE.md` (implementation contract)

This spec is allowed to be incomplete. Prefer opening issues that extend this spec over making untracked assumptions in code.

## System overview

Components:

- Vue dashboard (`faron-frontend/`)
- Ktor backend + Postgres (`ranelle-backend/`)
- Pico/RP2040 firmware (separate codebase; HTTP client)
- Dockerized production stack on a DIY homeserver

Data flow (current assumptions):

- Firmware pushes sensor readings to backend via HTTP `POST`.
- Firmware pulls configuration/parameters from backend via HTTP `GET`.
- Dashboard talks directly to backend over HTTP on the LAN.
- Mapping: 1 device <-> 1 plant (for now).

## Production stack goal

Target deployment:

- Runs on a DIY homeserver.
- Backend runs in a Docker container.
- Frontend runs in a Docker container.
- Postgres runs in a Docker container with a persistent volume.
- Images are built from `main` and published to a container registry.
- Homeserver pulls released images and updates when a new version is released.
- Frontend is reachable through Cloudflare Tunnel.
- Backend is not exposed publicly unless explicitly needed; frontend should reach it through an internal Docker network or a controlled public route.

Release/update model:

- `main` is the deployable branch.
- A release/tag produces immutable image tags.
- Homeserver updates should use versioned image tags, not only `latest`.
- Rollback should be possible by pinning the previous image tag.

Open questions:

- Which registry: GitHub Container Registry (`ghcr.io`) is the likely default.
- Which updater: Watchtower, Renovate-triggered deploy, manual `docker compose pull && up -d`, or another tool.
- Whether the backend should also be exposed through Cloudflare Tunnel or stay private behind the frontend/reverse proxy.

## Authentication / access control

Goal: only a single allowed Google email (or small allowlist) may access the dashboard.

Approach: backend-handled Google OAuth + server-side session cookie.

Requirements:

- Allowlist enforced server-side (env/config), not in frontend code.
- Frontend uses cookie-based session; no OAuth tokens stored in the browser.

API contract:

- `GET /api/v1/me` -> `200 { "email": "..." }` when logged in, `401` when not.
- `POST /api/v1/logout` -> clears session.
- OAuth endpoints (backend-owned):
  - `/auth/google/login` (redirect)
  - `/auth/google/callback`

## Device connection settings

Goal: user can enter the connection info needed to identify/talk to the microcontroller node.

Draft fields:

- `deviceId` (string)
- `nodeIp` (string)
- `nodePort` (number)

Storage:

- Backend persists settings in Postgres (or a config table) so they survive restarts.
- Frontend does not store settings as the source of truth.

API contract:

- `GET /api/v1/settings`
- `PUT /api/v1/settings`

Open question:

- Confirm what "node ip/port" points to (Pico endpoint vs gateway). Rename fields when clarified.

## Plants (catalog + persistence)

Goal: user adds plants from an external catalog (Perenual preferred), stores them locally, and can delete them.

Rules:

- Backend owns external API keys and performs catalog HTTP requests (avoid exposing keys in browser).
- Local plant record stores `externalId` (catalog identifier) plus local fields (custom name, etc).

Backend API (v1):

- `GET /api/v1/plants`
- `POST /api/v1/plants`
- `DELETE /api/v1/plants/{id}`

Catalog proxy (v1):

- `GET /api/v1/catalog/plants/search?q=...`
- `GET /api/v1/catalog/plants/{externalId}`

UI behaviors:

- Plants list page with Add + Delete.
- Plant details page shows catalog-derived requirements (moisture + light) and stored plant fields.

## Sensor readings (moisture + light)

Goal: store time-series moisture and light intensity readings and visualize them in the dashboard.

Firmware -> backend ingest:

- `POST /api/v1/readings` with at least:
  - `deviceId`
  - `capturedAt`
  - `soilMoisture`
  - `lightLux` (or other chosen unit; must be consistent)

Dashboard queries:

- `GET /api/v1/readings/latest?deviceId=...`
- `GET /api/v1/readings/timeseries?deviceId=...&from=...&to=...&metric=soilMoisture`
- `GET /api/v1/readings/timeseries?deviceId=...&from=...&to=...&metric=lightLux`

Chart UX:

- Show at least last 24h and last 7d.
- Clear empty state when no readings exist.

## Dashboard pages (MVP)

1. Login (Google OAuth)
2. Plants list (view + add + delete)
3. Plant details (requirements + charts)
4. Settings (device connection info)

## Backend requirements (to support frontend wiring)

- Versioned API under `/api/v1` for all new endpoints; migration of any existing unversioned endpoints must be explicit.
- CORS configured for Vite dev server during local development.
- `GET /api/v1/health` for smoke testing.
- Minimal JSON error shape where a body is returned: `{ "error": "..." }`.

## Deployment requirements

- Provide production Dockerfiles for frontend and backend.
- Provide a homeserver `docker-compose.yml` (or equivalent) for frontend, backend, Postgres, and optional Cloudflare tunnel container.
- Use env vars/secrets for:
  - Postgres credentials
  - Google OAuth client credentials
  - allowed Google email(s)
  - external plant catalog API key
  - Cloudflare tunnel token/config
- Keep persistent data in Docker volumes (especially Postgres).
- Document local dev compose separately from production compose if they diverge.
