# Multi-tenancy — architecture and approach

**Canonical overview** for Healenium Pro multi-tenancy (shared schema + PostgreSQL RLS).

Detail guides and delivery plan: see [README](./README.md) · [implementation-plan.md](./implementation-plan.md).

---

## 1. Product model

| Edition | Distribution | Multi-tenancy |
|---------|--------------|---------------|
| **Free (OSS)** | Public backend / proxy / examples | Single default tenant, no RLS |
| **Pro** | Paid stack including **healenium-ai** | Many tenants + RLS + SaaS isolation |

**healenium-ai** lives in a **private repository**. Pro customers get sources and/or images. That is the **primary commercial gate**: Free installs do not ship AI; Pro does.

Runtime does **not** need “call AI on every API request” to keep multi-tenancy paid.

Secondary gates:

1. Spring profile `pro` vs `free`
2. Pro-only Liquibase (RLS and, ideally, multitenancy schema)
3. Deploy topology (private network, M2M keys, Helm ships AI only in Pro)

Weak alone: flipping `@Profile("pro")` in an open jar, mocking validators in source.

---

## 2. Planes

```
┌──────────────────────────────────────────────────────────────┐
│ CONTROL PLANE — healenium-ai (private / Pro-only)            │
│  • Create / suspend / resume tenants                         │
│  • Sync tenant rows → backend `tenants`                      │
│  • AI inference                                              │
└─────────────────────────────┬────────────────────────────────┘
                              │ sync (rare)
┌─────────────────────────────▼────────────────────────────────┐
│ DATA PLANE — healenium-backend                               │
│  • Local `tenants` registry + ACTIVE check                   │
│  • TenantFilter: Healenium-Tenant-Id                                 │
│  • SET LOCAL app.tenant_id + PostgreSQL RLS                  │
│  • No Cognito JWT                                            │
└─────────────────────────────▲────────────────────────────────┘
                              │ Healenium-Tenant-Id + M2M headers
┌─────────────────────────────┴────────────────────────────────┐
│ AUTH PLANE — hlm-proxy (+ edge)                              │
│  • Cognito JWT (UI)                                          │
│  • membership → allowed tenants                              │
│  • WebDriver session / capability → tenant                   │
│  • Sets Healenium-Tenant-Id (+ internal token)                       │
│ ALB / WAF: x-api-key for CI                                  │
└──────────────────────────────────────────────────────────────┘
```

| Question | Owner |
|----------|--------|
| Does tenant exist and is ACTIVE? | Backend local `tenants` (synced from AI) |
| May this caller use this tenant? | Proxy (JWT + membership) or M2M contract |
| Are rows isolated? | PostgreSQL RLS |
| Who creates tenants? | healenium-ai |
| Is deploy entitled to Pro? | Private AI access (repo / images) |

---

## 3. Why not validate tenants via healenium-ai on every request?

Mixing two concerns:

| Concern | Tool |
|---------|------|
| **Commercial** — Free must not get Pro stack | Private AI + Pro deploy |
| **Runtime** — fast “is ACTIVE?” | Local DB (+ short cache) |

Per-request AI on the data path makes AI a SPOF, adds latency, and duplicates registries without a clear sync story.

---

## 4. Runtime design

### Hot path (backend)

```text
Healenium-Tenant-Id → UUID → local tenants ACTIVE? → TenantContext
                 → ProTenantTx: SET LOCAL app.tenant_id → RLS
```

- Missing / bad UUID → `400`
- Unknown / non-ACTIVE → `403`
- **No** healenium-ai HTTP on this path

### Lifecycle (control → data)

```text
AI create/suspend → sync/UPSERT backend.tenants → (optional cache invalidate)
```

Production tenants are not invented only in backend. Default UUID `00000000-0000-0000-0000-000000000001` stays for migrations / Free / upgrades.

### Auth

| Client | Path | Tenant | Auth |
|--------|------|--------|------|
| UI | UI → proxy → backend | Proxy sets `Healenium-Tenant-Id` | JWT on **proxy** |
| WebDriver | proxy → backend | capability / session | Edge / internal token |
| Mobitru | direct → backend | Client header | `Healenium-Api-Key` when M2M on |

Backend: `permitAll` + optional M2M filter + `TenantFilter` + RLS. No JWT resource server.

---

## 5. Free vs Pro (technical)

### Profiles

| Profile | Behaviour |
|---------|-----------|
| `free` | No `TenantFilter`, `FreeTenantTx`, no RLS session vars |
| `pro` | Filter + local ACTIVE + `ProTenantTx` + RLS + optional M2M |

### Liquibase (target)

| Changelog | Contents | Who |
|-----------|----------|-----|
| `changelog-core.xml` | Tables 001–010 | Free + Pro |
| `changelog-main.xml` | core + `016` `tenant_id` columns only | **Free** |
| `changelog-multitenancy.xml` | `011` registry, `013`/`014` membership, `012`/`015` RLS | **Pro** |
| `changelog-pro.xml` | core + multitenancy | **Pro** |

Free has `tenant_id` columns (JPA shared with Pro) but **no** `tenants` registry, membership, or RLS. Upgrade: [free-to-pro-upgrade.md](./free-to-pro-upgrade.md).

### Formula

> **Private healenium-ai sells Pro. Backend keeps a synced local tenant registry and enforces RLS. Proxy decides who may use which tenant. No AI round-trip on the data-plane hot path.**

---

## 6. End-to-end flows

**Provisioning:** AI creates tenant → sync to backend → membership for UI users → clients use UUID as `Healenium-Tenant-Id`.

**UI request:** Cognito → proxy (JWT + membership) → backend with internal token + `Healenium-Tenant-Id` → local ACTIVE → RLS. **Zero AI calls.**

**Suspend:** AI status change → sync → next API → `403`.

**Free OSS:** `profile=free`, main changelog (target), no AI, no multi-tenant filter/RLS.

---

## 7. Protection (honest)

| Layer | Mechanism |
|-------|-----------|
| Distribution | Private healenium-ai sources/images |
| Schema | RLS in Pro changelog |
| Runtime | Local `tenants` + filter |
| Edge | M2M + NetworkPolicy |

Private AI is the **product lock**. Local validation + RLS are **runtime engineering**.

---

## 8. Implementation roadmap

| Phase | Focus | Status |
|-------|--------|--------|
| **0** | Local ACTIVE in `TenantFilter`; demote WebClient-per-request AI | **Done** |
| **1** | AI → backend tenant sync API (M2M) | **Done** — `PUT /internal/tenants` (+ `/bulk`); AI `BackendTenantSyncClient` on create/`PATCH …/status` + `POST /tenants/sync`; retries; internal M2M when token configured |
| **2** | Liquibase Free/Pro hygiene | **Done** — Free=`changelog-main` (core+016 columns); Pro=`changelog-pro` (core+multitenancy); [free-to-pro-upgrade.md](./free-to-pro-upgrade.md) |
| **3** | Proxy: JWT, membership, `/me`, forward only tenant + internal token | **Done** — `healenium-proxy` (`HEALENIUM_AUTH_ENABLED`, `TenantAuthGlobalFilter`, `/hlm-proxy/me`, WD `RestClientTenantAuth`) |
| **4** | UI clients: `/me`, Bearer, `Healenium-Tenant-Id` | **Partial** — interceptors + bootstrap; SPA OIDC (PKCE) still optional; membership provision via `PUT /internal/membership` |
| **5** | Infra: `healenium_app`, M2M, NetworkPolicy | **Done** — `aws-healenium-pro` Phase E (`docs/multitenancy-phase-e.md`) |
| **6** | Staging E2E / Free regression | **Open** — Phase F |

---

## 9. Decision log

| Decision | Choice |
|----------|--------|
| JWT on backend? | **No** — proxy |
| Validate tenant via AI every request? | **No** — local DB |
| Who creates tenants? | **healenium-ai** |
| Who stores ACTIVE for API filtering? | **backend `tenants`** |
| Primary Free vs Pro lock | **Private AI + Pro deploy** |

---

## Detail documents

| Document | Topic |
|----------|--------|
| [db-schema.md](./db-schema.md) | `tenants` table + `tenant_id` columns |
| [db-rls.md](./db-rls.md) | PostgreSQL RLS policies |
| [db-roles.md](./db-roles.md) | `healenium_app` vs admin / Liquibase |
| [app-tenant-filter.md](./app-tenant-filter.md) | HTTP filter + local ACTIVE check |
| [app-set-local.md](./app-set-local.md) | `ProTenantTx` / `FreeTenantTx` |
| [app-entities.md](./app-entities.md) | Persist `tenant_id` on entities |
| [backend-contract.md](./backend-contract.md) | Headers, M2M, operational contract |
| [tests.md](./tests.md) | Integration tests for isolation |
