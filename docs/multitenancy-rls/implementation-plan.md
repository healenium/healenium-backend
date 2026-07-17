# Implementation plan — Multi-tenancy (Pro)

Based on [architecture.md](./architecture.md) and detail guides in this folder.

**Goal:** ship shared-schema multi-tenancy with RLS for Pro, Free remaining single-tenant; private healenium-ai as product gate; JWT on proxy; backend as data plane.

---

## Current status (baseline)

| Area | Status |
|------|--------|
| Schema `tenants` + `tenant_id`, RLS (`012` + `015` NULLIF) | Done (Pro changelog) |
| `ProTenantTx` / `FreeTenantTx`, entity listener | Done |
| Local ACTIVE in `TenantFilter` + Caffeine | Done |
| Remove AI HTTP from hot path | Done |
| `PUT /internal/tenants` sync UPSERT + status allow-list | Done |
| `/internal/**` M2M when token/keys configured | Done |
| AI pushes sync after create / status change | Done (`BackendTenantSyncClient`) |
| Liquibase Free without registry/membership/RLS | **Done** (`016` columns only on Free) |
| Proxy JWT + membership + `/me` | **Done** (`HEALENIUM_AUTH_ENABLED`, `TenantAuthGlobalFilter`, `GET /hlm-proxy/me`) |
| UI interceptors / Mobitru M2M in prod | **UI Partial** (tenant + Bearer headers; SPA OIDC open); Mobitru/Helm keys → Phase E |
| Helm / NetworkPolicy / M2M secrets | **Done** (aws-healenium-pro Phase E — see `docs/multitenancy-phase-e.md`; Pro always enforces M2M) |
| Membership write + multi-tenant unique | **Done** (`PUT`/`DELETE /internal/membership`, changelog `017`) |
| Screenshot tenant scoping | **Done** (`ImageController` + report RLS) |
| AI sync retries + bootstrap | **Done** (`POST /tenants/sync`, bulk upsert, retries) |
| Staging E2E (Phase F) | **Open** |

---

## Phase map

```text
Phase A  Backend harden + AI sync          (control → data)
Phase B  Liquibase Free/Pro hygiene
Phase C  hlm-proxy auth plane
Phase D  UI + Mobitru + examples
Phase E  Infra / SaaS (Helm, keys, nets)
Phase F  E2E verification & rollout
```

Phases A–B are mostly **healenium-backend** (+ **healenium-ai** for sync).  
C–D are other repos. E is **aws-healenium-pro** / K8s. F cuts across.

---

## Phase A — Complete control-plane sync (AI → backend)

**Docs:** [architecture §4.2](./architecture.md), [backend-contract.md](./backend-contract.md)

### A1. healenium-ai: sync client

| Task | Detail | Done when |
|------|--------|-----------|
| Config | `BACKEND_INTERNAL_URL`, sync service key / internal token | Config documented |
| After `POST /tenants` | `PUT {backend}/internal/tenants` with `{id,name,status}` | Backend row appears |
| After status change | Same UPSERT with new status | Suspend → backend `SUSPENDED`/`DISABLED` |
| Failure policy | Retry + log/alert; do not silently drift | Retry/metrics defined |
| Auth | `Healenium-Internal-Token` (same secret as proxy↔backend) | 401 without token when enforce on |

### A2. Backend: harden internal API

| Task | Detail | Done when |
|------|--------|-----------|
| Status allow-list | Accept only known statuses (`ACTIVE`, `SUSPENDED`/`DISABLED`, …) | Invalid → `400` |
| Idempotency | Repeated PUT safe | Covered by test |
| Optional GET | `GET /internal/tenants/{id}` for AI/ops debug | Optional |
| M2M for `/internal/**` | Always require M2M for internal even if broad enforce off — or document “dev only open” | Prod cannot upsert publicly |
| Tests | Sync service IT + MockMvc for controller | Green |

### A3. Ops bootstrap

| Task | Detail | Done when |
|------|--------|-----------|
| Seed path | Existing SaaS tenants: one-shot sync or SQL/admin | All ACTIVE tenants in both sides |
| Doc curl examples | Already in contract; add AI→backend sequence | In README/ops notes |

**Exit criteria:** create/suspend in AI always leaves backend `tenants` consistent; API with that UUID works/fails ACTIVE correctly without AI on hot path.

---

## Phase B — Liquibase Free / Pro hygiene

**Docs:** [architecture §5](./architecture.md), [db-schema.md](./db-schema.md)

| Task | Detail | Done when |
|------|--------|-----------|
| B1. Split includes | Free `changelog-main` = core + `016` columns; Pro = core + multitenancy | Fresh Free has no `tenants`/`membership`/RLS |
| B2. Upgrade path | [free-to-pro-upgrade.md](./free-to-pro-upgrade.md) | Runbook exists |
| B3. Default tenant | Kept in `011` / `016` as `00000000-…-0001` | Backfill works |
| B4. Membership | `013`/`014` Pro-only via `changelog-multitenancy` | Clear |
| B5. CI | Pro `MultitenancyRlsIntegrationTest` green after split | Green |

**Risk addressed:** existing Free that already applied full `011` keeps leftover tables (no drop). New Free installs get columns only.

**Exit criteria:** met — see Liquibase table in [architecture.md](./architecture.md).

---

## Phase C — hlm-proxy auth plane

**Docs:** [architecture §2 / §4.3](./architecture.md), [app-tenant-filter.md](./app-tenant-filter.md)

| Task | Detail | Done when |
|------|--------|-----------|
| C1. JWT validation | Cognito resource-server when `HEALENIUM_AUTH_ENABLED=true` | Invalid Bearer → 401 |
| C2. Membership resolve | Proxy `MembershipClient` → backend `GET /internal/membership?issuer&sub` + Caffeine | No membership → 403 |
| C3. Tenant selection | Single tenant auto; multi → require `Healenium-Tenant-Id` ∈ list | Spoof other tenant blocked |
| C4. Forward to backend | `TenantAuthGlobalFilter`: set tenant + internal token; strip `Authorization` | Backend sees only M2M + tenant |
| C5. `/me` | Proxy `GET /hlm-proxy/me` | UI bootstrap without backend JWT |
| C6. WebDriver path | Capability `hlm:tenant_id` (legacy `hlm:api_key`); `RestClientTenantAuth` adds tenant + internal token on restore | Session APIs tenant-scoped under Pro M2M |
| C7. Tests | `TenantAuthGlobalFilterTest` (+ backend `MembershipInternalControllerTest`) | Green |

**Exit criteria:** met for proxy auth plane (default auth off for local/dev). Next: Phase D UI.

**Enable (Pro):** `HEALENIUM_AUTH_ENABLED=true`, `COGNITO_ISSUER_URI=…`, `M2M_INTERNAL_TOKEN=…` (same secret as backend/AI).

---

## Phase D — Clients (UI, Mobitru, examples)

| Component | Tasks | Done when |
|-----------|--------|-----------|
| **hlm-ui-dashboard** | Bootstrap `/hlm-proxy/me` when Bearer present; store tenant; axios sends `Authorization` + `Healenium-Tenant-Id` | **Done** — see below |
| **Mobitru** | Direct backend: `Healenium-Tenant-Id` + `Healenium-Api-Key`; document keys | Documented in [backend-contract.md](./backend-contract.md) |
| **Examples** | Maven / Playwright: capability `hlm:tenant_id` | Maven comment in `ProxyDriver`; Playwright same header/capability when wired |

### UI (`hlm-ui-dashboard`)

| Piece | Detail |
|-------|--------|
| Bootstrap | `src/auth/bootstrapTenant.ts` on App mount |
| Storage | `healenium.tenantId` / `healenium.accessToken` (sessionStorage); env `VITE_DEFAULT_TENANT_ID`, `VITE_ACCESS_TOKEN` |
| Headers | `attachTenantAuthInterceptor` on default axios + Report `axios.create` instances |
| Flag | `VITE_TENANT_AUTH_ENABLED=true` → 403 toasts instead of Cognito redirect |
| Proxy JWT on | Set token + `HEALENIUM_AUTH_ENABLED=true` on proxy; UI calls `/hlm-proxy/me` |
| Proxy JWT off | Set `VITE_DEFAULT_TENANT_ID` only (membership not enforced on proxy) |

Full Cognito SPA (PKCE) to obtain Bearer in-browser is **not** bundled yet — ALB cookie login alone does not give JS a JWT. Use `healenium.accessToken` / `VITE_ACCESS_TOKEN` until SPA OIDC is added.

### WebDriver examples

```java
options.setCapability("hlm:tenant_id", "<tenant-uuid>");
// legacy: options.setCapability("hlm:api_key", "<tenant-uuid>");
```

Proxy `InitSession` + `RestClientTenantAuth` forward tenant + `Healenium-Internal-Token` to backend.

### Mobitru

```http
Healenium-Tenant-Id: <tenant-uuid>
Healenium-Api-Key: <key from M2M_API_KEYS>
```

Pro always requires M2M (`M2M_INTERNAL_TOKEN` and/or `M2M_API_KEYS`).

---

## Phase E — Infra / SaaS

| Task | Detail | Done when |
|------|--------|-----------|
| E1. DB roles | Runtime `healenium_app` (no BYPASSRLS); Liquibase `healenium_user` | **Done** — `startup/ensure-healenium-app-role.sh`, Pro values `postgresql.endpoint.user=healenium_app` |
| E2. Env | `M2M_API_KEYS`, `M2M_INTERNAL_TOKEN`, AI/backend URLs (Pro always enforces M2M) | **Done** — wired in backend/proxy/AI deploys + `hlm-secret` keys |
| E3. Network | Backend `/internal/**` only from AI + proxy; AI private | **Done** — NetworkPolicies + ClusterIP for backend/AI on Pro |
| E4. Images | Pro chart includes healenium-ai; Free chart does not | **Done** — `hlmai.enable` |
| E5. WAF / ALB | Cognito UI; `x-api-key` CI; WAF ≠ app M2M | **Documented** — `aws-healenium-pro/docs/multitenancy-phase-e.md` |

**Runbook:** `aws-healenium-pro/docs/multitenancy-phase-e.md` (sibling repo).

---

## Phase F — Verification & rollout

| Task | Detail |
|------|--------|
| F1. Backend tests | Keep unit + `MultitenancyRlsIntegrationTest`; add filter MockMvc / sync IT |
| F2. Contract checklist | Missing header `400`; inactive `403`; cross-tenant empty; M2M `401` |
| F3. Staging E2E | Provision AI → sync → UI login → heal/report for tenant A only |
| F4. Suspend E2E | Suspend in AI → sync → UI/API `403` |
| F5. Free regression | `profile=free` single-tenant paths unchanged |
| F6. Rollout | Staging → limited Pro tenants → production; rollback = disable M2M / freeze sync (document) |

---

## Suggested order (critical path)

```text
A1–A2 (AI sync)  →  C1–C5 (proxy JWT + /me)  →  D UI
       ↘
         E2–E3 (M2M + network) in parallel once A2 ready
B (Liquibase) can run parallel to C (low coupling)
F after A+C+D minimal slice
```

**MVP slice (first production-usable):**

1. A complete (AI sync)  
2. C WebDriver + internal token (if WD already has tenant capability) **or** C UI JWT path  
3. E M2M + `healenium_app`  
4. F staging E2E for that path  

UI JWT (C1–C5 + D) can follow WD/Mobitru if SaaS CI is priority.

---

## Repo ownership

| Repo | Phases |
|------|--------|
| **healenium-backend** | A2, B, F1–F2 (backend), support membership internal API if needed for C2 |
| **healenium-ai** | A1, A3 |
| **healenium-proxy** | C |
| **hlm-ui-dashboard** | D (UI) |
| **aws-healenium-pro** / kubernetes | E |
| **examples** | D |

---

## Explicit non-goals (this plan)

- JWT resource server on healenium-backend  
- Per-request `GET` tenant validation to AI  
- License heartbeat / `healenium.license.*`  
- Cross-tenant admin HTTP API (use DB admin role)

---

## Tracking checklist (copy to issue tracker)

- [x] A1 AI sync on create/suspend  
- [x] A2 Backend status validation + M2M for `/internal`  
- [x] A3 Bootstrap / seed existing tenants (`POST /tenants/sync` → `PUT /internal/tenants/bulk`)  
- [x] B1–B5 Liquibase Free/Pro split + upgrade doc (Pro IT green; Free changelog smoke via Liquibase entrypoint)  
- [x] C1–C7 Proxy JWT, membership, `/me`, WD forward (`healenium-proxy`; auth off by default)  
- [x] D UI tenant bootstrap + axios headers (`hlm-ui-dashboard`); Mobitru/examples documented  
- [x] E1–E5 Helm, roles, M2M, network (`aws-healenium-pro`)  
- [x] Membership write API (`PUT`/`DELETE /internal/membership`) + multi-tenant unique (017)  
- [x] Screenshot access scoped via report + RLS (`ImageController`)  
- [x] AI sync retries + Pro `failOnError`  
- [ ] F1–F6 Staging E2E + Free regression + rollout  
- [ ] D SPA Cognito OIDC (PKCE) for in-browser Bearer (UI still uses env/`sessionStorage` token)  

Reference docs: [architecture.md](./architecture.md) · [backend-contract.md](./backend-contract.md) · [app-tenant-filter.md](./app-tenant-filter.md) · [db-schema.md](./db-schema.md) · [db-rls.md](./db-rls.md) · [tests.md](./tests.md)
