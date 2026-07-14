# Security

HomeCloud Planner is built for one deployment shape: a single administrator,
running on a home local network, not exposed to the public internet. This
document covers what's in place, what's deliberately out of scope, and what
a deployer needs to do. The authentication design itself is covered in
depth in [ADR-0008](decisions/ADR-0008-authentication-approach.md); this
document is the operational checklist around it.

## Deployment model: local network only

This application has no rate limiting, no intrusion detection, no
web application firewall, and (by default) no TLS. It is not designed or
tested to be exposed directly to the public internet — do not port-forward
it, and do not put it on a host reachable from outside your LAN. If you
want remote access, put it behind a VPN (e.g. WireGuard, Tailscale) to your
home network rather than exposing the ports directly.

If your network segments IoT/guest devices from trusted devices (VLANs),
run this on the trusted segment, since anyone who can reach the backend's
port and knows (or guesses) the admin password can read and change every
project's data.

## The admin account

There is exactly one account, created once at first startup from the
`ADMIN_USERNAME` and `ADMIN_PASSWORD` environment variables — see
`.env.example` and ADR-0008. There is no default username or password
baked into the application; if these variables are unset when the backend
first starts against an empty database, no account is created and login
will not work until you set them and restart.

- **Change the password before first real use.** `.env.example` ships a
  placeholder (`local-dev-password`-style) value purely so the stack
  starts up runnable; treat it the same as any other example credential —
  never rely on it past initial local testing.
- **Recovering a lost password** requires direct database access: delete
  the single row in the `admin_user` table, then restart the backend with
  the environment variables set to the new credentials —
  `AdminAccountInitializer` only acts when the table is empty.
- Passwords are hashed with BCrypt (`BCryptPasswordEncoder`) before
  storage; the plaintext value is never persisted.

## Session cookies and CSRF

- The session cookie (`JSESSIONID`) is `httpOnly` always, and `Secure`
  only when `SESSION_COOKIE_SECURE=true` is set — which you should do if
  and only if you've put TLS in front of the backend (e.g. a reverse
  proxy terminating HTTPS on your LAN). Left at the default (`false`),
  it's sent over plain HTTP, which is the expected mode for a typical LAN
  deployment of this app.
- The CSRF cookie (`XSRF-TOKEN`) is deliberately **not** `httpOnly` — the
  frontend JavaScript has to read it to echo it back as the
  `X-XSRF-TOKEN` header on mutating requests (the standard double-submit
  pattern for a SPA). This is not a bug; the CSRF cookie's value on its
  own grants no access without a valid session.
- **The frontend and backend must be reached via the same hostname**
  (differing only by port) for the session and CSRF cookies to be sent on
  cross-origin API calls at all — see the `SameSite=Lax` note below. If
  you deploy behind a reverse proxy that puts both under one origin
  (e.g. `/` for the frontend, `/api` proxied to the backend), that also
  works and is simpler for cookie handling, but isn't required.
- Both cookies use `SameSite=Lax`. Two different ports on the same
  hostname (e.g. `192.168.1.50:3000` and `192.168.1.50:8080`) count
  as the same "site" for `SameSite` purposes even though they're
  different origins for CORS — that's what makes cross-port cookie auth
  work here without needing `SameSite=None` (which would additionally
  require `Secure`, i.e. HTTPS).

## CORS

`CORS_ALLOWED_ORIGINS` (comma-separated) is the exhaustive allowlist of
origins the backend will accept credentialed cross-origin requests from —
see `CorsConfig`. Update it to match wherever your frontend is actually
served from (e.g. `http://192.168.1.50:3000` for a LAN deployment); an
origin not on this list gets no CORS headers and the browser blocks the
response.

## Security headers

Configured in `SecurityConfig` and `application.yml`:

- `X-Frame-Options: DENY` — this app is never meant to be framed by
  another site.
- Request body size limits (`server.tomcat.max-http-form-post-size`,
  `max-swallow-size`, both 2MB) — there is no file-upload feature, so
  request bodies are expected to be small JSON payloads only.
- Spring Security's other defaults (`X-Content-Type-Options: nosniff`,
  cache-control headers on sensitive responses) are left at their
  out-of-the-box values, which are appropriate here.

The frontend's nginx container (`frontend/nginx.conf`) sets the same
`X-Content-Type-Options` and `X-Frame-Options` headers, plus
`Referrer-Policy: same-origin`, for the static assets it serves.

## Actuator exposure

Only `/actuator/health` is exposed
(`management.endpoints.web.exposure.include: health`), and it reports no
details (`management.endpoint.health.show-details: never`) — it returns
`{"status": "UP"}` or `{"status": "DOWN"}` and nothing else, no matter who
asks. Every other actuator endpoint (env, beans, mappings, threaddump,
...) is not mapped at all, authenticated or not.

## Docker container users and permissions

- The backend container runs as a dedicated non-root user (`planner`),
  created explicitly in `backend/Dockerfile` (`addgroup -S planner &&
  adduser -S planner -G planner`), not the image's default root user.
- The frontend container is `nginx:1.27.4-alpine` unmodified: the master
  process starts as root (needed to bind port 80 and manage worker
  processes) but nginx's worker processes — the ones actually parsing
  request data — drop to the image's built-in unprivileged `nginx` user
  by default. This is nginx's standard, expected security posture and
  wasn't changed.
- Postgres's official image similarly runs its server process as the
  non-root `postgres` user by default.

## Postgres exposure

The base `compose.yaml` does **not** publish Postgres's port to the host —
only the backend container can reach it, over the compose-internal
network. `compose.override.yaml` (merged in automatically by `docker
compose`, no flag needed) adds the `5432:5432` host port mapping for local
development convenience (connecting a SQL client from your host machine).
**Do not carry `compose.override.yaml` into a production-style
deployment** — omit it (e.g. `docker compose -f compose.yaml up -d`) if
Postgres shouldn't be reachable from outside the compose network at all.

## What's explicitly out of scope

Per the build plan for this milestone, none of the following exist, and
adding them would be scope creep for a single-admin LAN app:

- User registration or invitation flow
- Password reset (self-service — see "Recovering a lost password" above
  for the manual path)
- Social login
- Multi-user accounts or per-user authorization
- File uploads (and therefore no upload-specific validation, virus
  scanning, or storage-quota concerns)
- Rate limiting / brute-force lockout on the login endpoint

See [ADR-0008](decisions/ADR-0008-authentication-approach.md) for the
documented (not yet implemented) path to Authentik/OpenID Connect, which
is the intended way multi-user support would eventually arrive.
