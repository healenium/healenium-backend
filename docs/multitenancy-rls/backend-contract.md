# Backend tenant contract (operational)

Parent: [architecture.md](./architecture.md).

Contract for **healenium-backend** as the **data plane**.

---

## Editions

| Edition | Profile | `Healenium-Tenant-Id` | Isolation |
|---------|---------|---------------|-----------|
| Free | `free` | Ignored | Default tenant; no RLS |
| Pro | `pro` | Required (except skips) | Local ACTIVE + RLS |

Default tenant: `00000000-0000-0000-0000-000000000001`.

---

## Pro data path

Callers send:

```http
Healenium-Tenant-Id: <tenant-uuid>
```

Backend:

1. Parse UUID → `400` if bad  
2. Local `tenants` ACTIVE (optional cache) → `403` if not  
3. `TenantContext`  
4. `ProTenantTx` → `SET LOCAL app.tenant_id`  
5. RLS  

No healenium-ai call on this path. Rows synced from AI via:

```http
PUT /internal/tenants
Content-Type: application/json
Healenium-Internal-Token: <shared-secret>   # required when M2M_INTERNAL_TOKEN is set

{ "id": "<uuid>", "name": "Acme", "status": "ACTIVE" }
```

Bulk bootstrap (A3):

```http
PUT /internal/tenants/bulk
Content-Type: application/json
Healenium-Internal-Token: <shared-secret>

[
  { "id": "<uuid>", "name": "Acme", "status": "ACTIVE" },
  { "id": "<uuid>", "name": "Beta", "status": "DISABLED" }
]
```

Or from AI: `POST /tenants/sync` (pushes all AI tenants to bulk).

Allowed statuses: `ACTIVE`, `DISABLED` (case-insensitive). Invalid → `400`.

Path is skipped by `TenantFilter`. On Pro, M2M is always required for data and `/internal/**` paths (except actuator/swagger).

healenium-ai syncs automatically after `POST /tenants` and `PATCH /tenants/{id}/status` (`healenium.tenant-sync.*`).

### Membership (UI identities)

```http
PUT /internal/membership
Healenium-Internal-Token: <shared-secret>
Content-Type: application/json

{ "issuer": "https://cognito-idp…", "externalSub": "<sub>", "tenantId": "<uuid>", "role": "owner" }
```

```http
DELETE /internal/membership?issuer=…&sub=…&tenantId=<uuid>
GET  /internal/membership?issuer=…&sub=…
```

One identity may belong to multiple tenants (`UNIQUE(issuer, external_sub, tenant_id)`).

### Skip paths

`/actuator/**`, `/swagger/**`, `/v3/api-docs/**`

---

## Authentication

- **No JWT on backend** — Cognito / UI bootstrap on **hlm-proxy**  
- Proxy always sends `Healenium-Internal-Token` + `Healenium-Tenant-Id` on Pro  
- `Healenium-Tenant-Id` alone is incomplete auth; combine M2M, network, RLS  

### M2M (always on for Pro profile)

| Caller | Headers |
|--------|---------|
| Mobitru | `Healenium-Api-Key` or `Authorization: Bearer <key>` + `Healenium-Tenant-Id` |
| hlm-proxy | `Healenium-Internal-Token` + `Healenium-Tenant-Id` |

Env: `M2M_API_KEYS`, `M2M_INTERNAL_TOKEN`. There is no `M2M_ENFORCE` toggle — Pro registers `M2mAuthFilter` and always requires a valid credential.

### WebDriver (via hlm-proxy)

```text
capability hlm:tenant_id = <tenant-uuid>   # preferred
capability hlm:api_key   = <tenant-uuid>   # legacy alias
```

Proxy stores it on the session and sends `Healenium-Tenant-Id` + `Healenium-Internal-Token` on healing/backend calls.

### UI (`hlm-ui-dashboard`)

```http
Authorization: Bearer <cognito-access-token>   # when proxy HEALENIUM_AUTH_ENABLED=true
Healenium-Tenant-Id: <tenant-uuid>
```

Bootstrap: `GET /hlm-proxy/me` → `defaultTenantId`. Env: `VITE_DEFAULT_TENANT_ID`, `VITE_TENANT_AUTH_ENABLED`, `VITE_ACCESS_TOKEN` / `sessionStorage.healenium.accessToken`.

---

## Lifecycle

| Step | Owner |
|------|--------|
| Create / suspend | healenium-ai |
| ACTIVE registry for API | backend `tenants` (sync) |
| User ↔ tenant | membership (proxy) |
| Per-request ACTIVE | backend local check |

---

## DB user

Pro runtime: `healenium_app` (no `BYPASSRLS`). See [db-roles.md](./db-roles.md).

---

## Config sketch

```yaml
healenium:
  m2m:
    api-keys: ${M2M_API_KEYS:}
    internal-token: ${M2M_INTERNAL_TOKEN:}
  tenant:
    cache-ttl: PT10M
    cache-max-size: 10000
  ai:
    # Sync / inference — not per-request tenant GET
    base-url: ${HEALENIUM_AI_SERVICE:http://localhost:6565}
    service-key: ${HEALENIUM_AI_SERVICE_KEY:}
```
