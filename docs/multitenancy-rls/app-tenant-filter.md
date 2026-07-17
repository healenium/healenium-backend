# Application: HTTP tenant filter

Parent: [architecture.md](./architecture.md). Related: [backend-contract.md](./backend-contract.md).

## Goal

On **Pro**, take `Healenium-Tenant-Id`, ensure it is a known **ACTIVE** local tenant, set `TenantContext` for the request.

## Edition

| Profile | Behaviour |
|---------|-----------|
| `pro` | `TenantFilter` registered (`TenantConfiguration`) |
| `free` | No filter; single default tenant; header ignored |

## Who sends the header?

Backend does **not** derive tenant from JWT.

| Caller | Source of tenant |
|--------|------------------|
| Proxy (UI) | JWT + membership on proxy |
| Proxy (WebDriver) | Capability `hlm:tenant_id` (legacy `hlm:api_key`) → session + RestClient headers |
| Mobitru | Client (+ API key if M2M on) |

“May this principal use this tenant?” → auth plane. Filter → “is this UUID processable here?”.

## Contract

1. Header present → else `400`  
2. Valid UUID → else `400`  
3. Local `tenants` ACTIVE → else `403`  
4. Set `TenantContext` (+ MDC `tenant_id`); clear in `finally`  

**No** healenium-ai HTTP on this path. Optional Caffeine over local lookups; invalidate on sync/suspend when possible.

### Skip paths

`/actuator/**`, `/swagger/**`, `/v3/api-docs/**`

M2M (when enforced) runs before this filter — see [backend-contract.md](./backend-contract.md).

## Implementation sketch

1. `TenantContext` — ThreadLocal + clear every request  
2. `OncePerRequestFilter` — parse, local validation (`TenantRepository`), set/clear  
3. MDC for logging  

## Verification

- ACTIVE UUID → proceeds  
- Missing/malformed → `400`  
- Unknown/non-ACTIVE → `403`  
- Free → APIs without header  

## Pitfalls

- Uncleared ThreadLocal  
- Default-tenant fallback on all Pro requests  
- Reintroducing per-request AI validation  
- Treating header alone as full auth without proxy/M2M/network in prod  
