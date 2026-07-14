# ADR-0008: Authentication approach

## Status

Accepted

## Context

Milestone 7 asks for authentication appropriate to a single-household,
local-network application: one administrator account, no registration, no
password reset, no social login, no file uploads, and no multi-user
authorization model. The requirement is to stop an unauthenticated visitor
on the LAN from reading or changing project data, not to build a general
identity system. The plan also asks that a path to a real identity
provider (Authentik via OpenID Connect) be documented for later, without
being implemented now.

## Decision

- **Session-cookie authentication, not JWT.** Spring Security's
  `DaoAuthenticationProvider` + `HttpSessionSecurityContextRepository`
  authenticate `POST /api/v1/auth/login` and persist the resulting
  `SecurityContext` in the servlet session; the session ID travels as an
  `httpOnly` cookie the browser handles automatically. There is exactly
  one account, so there's no token-refresh, revocation-list, or
  distributed-verification problem a JWT would earn its complexity for —
  a session cookie is simpler to implement correctly and simpler to
  reason about for a single-admin app.

- **One local administrator account, created only from environment
  variables at first startup.** `AdminAccountInitializer`
  (`ApplicationRunner`) creates the account from `ADMIN_USERNAME` /
  `ADMIN_PASSWORD` the first time the app starts against a database with
  no existing `admin_user` row, BCrypt-hashing the password
  (`BCryptPasswordEncoder`). If either variable is unset, no account is
  created and login simply doesn't work until they're set and the app is
  restarted — there is no default username or password anywhere in the
  code or migrations. See `V6__admin_account.sql` and
  `AdminAccountInitializer`.

- **Cookie-based double-submit CSRF, not session-embedded tokens.**
  `CookieCsrfTokenRepository.withHttpOnlyFalse()` writes the token to a
  readable `XSRF-TOKEN` cookie; the frontend reads it and echoes it back
  as the `X-XSRF-TOKEN` header on every mutating request
  (`src/api/client.ts`). This is the standard pattern for a SPA that
  talks to its API over `fetch`/XHR rather than submitting HTML forms —
  it doesn't require the server to render the token into a page, and it
  works whether or not the client and API happen to be same-origin. A
  dedicated `CsrfCookieFilter` forces the (otherwise lazily-resolved)
  token to be read on every request, which is what actually gets the
  cookie onto the response; without it, `CsrfTokenRequestAttributeHandler`
  defers resolution indefinitely for an app with no server-rendered views
  to trigger it, and the frontend would never receive a cookie to send
  back.

- **RFC 9457 problem details for both 401 and 403, no login-page
  redirect.** `ProblemDetailAuthenticationEntryPoint` and
  `ProblemDetailAccessDeniedHandler` replace Spring Security's default
  HTML/redirect behavior, consistent with every other error response in
  this API (see the project's existing `ProblemDetail` convention). The
  frontend's `AuthContext` and `ProtectedRoute` are what turn a 401 into a
  redirect to `/login` — that's a client-side routing concern, not
  something the API should assume.

- **CORS as an explicit `CorsConfigurationSource` bean, shared by Spring
  Security.** `CorsConfig` previously implemented `WebMvcConfigurer`
  directly; Spring Security's `.cors(...)` needs a `CorsConfigurationSource`
  bean specifically, and `allowCredentials(true)` is required for the
  session cookie to be sent on cross-origin requests from the frontend's
  origin. One bean now backs both Spring MVC's and Spring Security's CORS
  handling, instead of two definitions that could drift apart.

## Alternatives considered

- **JWT (access token in a header or cookie).** Rejected for now: it adds
  signing-key management, expiry/refresh handling, and (if stored in a
  cookie for XSS-resistance) ends up needing the same CSRF protection a
  session cookie needs anyway — extra moving parts with no benefit for a
  single-account app that isn't distributed across services.
- **HTTP Basic auth.** Rejected: no server-side logout (the browser just
  keeps resending cached credentials until the tab closes), and no clean
  way to distinguish "wrong password" from "not logged in yet" in a SPA
  without prompting the browser's native credential dialog.
- **Spring Session backed by Redis/JDBC for horizontal session sharing.**
  Rejected as unnecessary: this app runs as one backend container on a
  home network; the in-memory `HttpSession` Tomcat already provides is
  sufficient, and adding a session store would be infrastructure with no
  corresponding problem to solve.
- **Implementing Authentik/OpenID Connect now instead of a local
  account.** Rejected for this milestone per the build plan — it's real
  infrastructure (a running Authentik instance, realm/client
  configuration, network reachability from the LAN) that doesn't yet
  exist in this project's scope, and multi-user authorization isn't
  wanted yet either. The local-account approach is deliberately built so
  that swapping in OIDC later doesn't require touching the data model:
  see below.

## Future Authentik / OpenID Connect integration (documented, not implemented)

When a real identity provider becomes available on the network, the
intended migration is:

1. Add `spring-boot-starter-oauth2-client` and configure Authentik as an
   OIDC provider (`spring.security.oauth2.client.registration.authentik.*`),
   pointing at Authentik's issuer URI.
2. Replace `SecurityConfig`'s `.authorizeHttpRequests` +
   `AuthenticationManagerConfig` wiring with `.oauth2Login(...)`, and
   replace `AuthController`'s manual `authenticationManager.authenticate(...)`
   call with the OIDC login redirect flow. `ProblemDetailAuthenticationEntryPoint`
   and `ProblemDetailAccessDeniedHandler` can stay as-is for the API's
   401/403 responses. This alone doesn't require the data model to grow a
   multi-user authorization concept — until that's asked for, the OIDC
   token's subject can map 1:1 to the existing single-admin identity.
3. `AdminUser`/`AdminUserRepository`/`AdminAccountInitializer` and
   `V6__admin_account.sql` would be retired at that point, along with the
   `ADMIN_USERNAME`/`ADMIN_PASSWORD` environment variables — Authentik
   becomes the sole source of identity.
4. The frontend's CSRF handling can likely be simplified or dropped
   depending on whether the OIDC flow ends up session-cookie-based
   (keep it) or token-based (reconsider), and `AuthContext`'s
   `/auth/session` check would be replaced by whatever session-status
   endpoint the chosen OIDC client library exposes.

This is a documented direction, not a commitment to a specific timeline —
see the Revisit criteria below.

## Consequences

A visitor with no valid session cookie gets a 401 problem-detail response
from every API route except `GET /api/v1/health`, `GET /actuator/health`,
and `POST /api/v1/auth/login`; the frontend never renders application
pages for them. There is no way to create a second account, reset a
forgotten password other than resetting `ADMIN_USERNAME`/`ADMIN_PASSWORD`
and clearing the `admin_user` table, or authorize different users
differently — all deliberate, matching the stated scope. Losing the
`ADMIN_PASSWORD` environment variable's original value doesn't lock
anyone out of an already-created account (it's only consulted when no
account exists yet), but there's also no self-service recovery path;
recovering requires direct database access to delete the row and letting
`AdminAccountInitializer` recreate it on the next restart with new
environment variables.

## Revisit criteria

Revisit if: a second person needs an account (multi-user authorization,
not just multi-user authentication, becomes necessary — the data model
has no per-user ownership concept today); Authentik becomes available on
the network and OIDC integration described above becomes worth doing; or
the app starts being reachable outside the local network, which would
change the risk calculus around plain-HTTP session cookies (see
`docs/security.md`).
