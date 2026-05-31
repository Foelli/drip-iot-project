# Full Stack Milestones

This checklist tracks large project milestones across frontend, backend, firmware, deployment, and operations.

## Milestone 1: Backend API foundation

- [ ] Expose new endpoints under `/api/v1`.
- [ ] Add `GET /api/v1/health`.
- [ ] Configure CORS for Vue dev server.
- [ ] Standardize JSON error responses.
- [ ] Add use case test harness.

## Milestone 2: Frontend dashboard foundation

- [ ] Replace Vite starter UI with dashboard shell.
- [ ] Add backend API client module.
- [ ] Add Google OAuth login flow using backend session cookie.
- [ ] Add plants list view.
- [ ] Add settings view for device connection fields.

## Milestone 3: Plant catalog workflow

- [ ] Add backend catalog proxy for Perenual or selected plant API.
- [ ] Store external catalog id with local plant record.
- [ ] Add frontend plant search/select/add flow.
- [ ] Add plant details view with catalog moisture/light requirements.
- [ ] Add delete plant flow from the dashboard.

## Milestone 4: Sensor readings + charts

- [ ] Add `sensor/*` backend module.
- [ ] Add readings ingest endpoint for firmware (`POST /api/v1/readings`).
- [ ] Store moisture and light readings in Postgres.
- [ ] Add latest-reading endpoint.
- [ ] Add timeseries endpoint for charts.
- [ ] Add dashboard charts for moisture and light timeline.

## Milestone 5: Firmware integration

- [ ] Define firmware JSON payloads for readings.
- [ ] Implement firmware HTTP POST for readings.
- [ ] Implement firmware polling for backend config.
- [ ] Add retry/backoff behavior for temporary backend outages.
- [ ] Verify one device maps to one plant for the first iteration.

## Milestone 6: Dockerized local stack

- [ ] Add backend Dockerfile.
- [ ] Add frontend Dockerfile.
- [ ] Add docker compose for backend + frontend + Postgres.
- [ ] Add persistent Postgres volume.
- [ ] Add env var examples for local stack.
- [ ] Verify full stack starts from a clean checkout.

## Milestone 7: Release images from `main`

- [ ] Add CI build for backend image.
- [ ] Add CI build for frontend image.
- [ ] Publish images to registry (likely GHCR).
- [ ] Tag releases with immutable version tags.
- [ ] Document rollback by pinning an older image tag.

## Milestone 8: Homeserver deployment

- [ ] Add production compose file for homeserver.
- [ ] Configure Postgres volume/backups.
- [ ] Configure env/secrets on homeserver.
- [ ] Pull versioned images from registry.
- [ ] Define update mechanism (manual compose pull, Watchtower, or another updater).
- [ ] Verify backend and frontend restart cleanly after update.

## Milestone 9: Cloudflare Tunnel exposure

- [ ] Configure Cloudflare Tunnel for frontend hostname.
- [ ] Decide whether backend remains private or is exposed through controlled route.
- [ ] Ensure OAuth callback URL matches public frontend/backend route.
- [ ] Verify dashboard is reachable externally.
- [ ] Verify backend is not unintentionally public.

