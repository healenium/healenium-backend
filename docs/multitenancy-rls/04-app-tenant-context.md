# 04 — Application tenant context (HTTP request → TenantContext)

## Goal
Extract the tenant identifier from each incoming request and make it available to the application layers.

## Tenant identifier contract
- Header: `X-Tenant-Id`
- Value: UUID string

If the header is missing or invalid:
- return `400 Bad Request` (or `403 Forbidden` depending on your API contract)

## Implementation steps

### 1) Create `TenantContext`
A minimal ThreadLocal-based holder:
- `setTenantId(UUID tenantId)`
- `UUID getTenantId()`
- `clear()`

Requirements:
- must be cleared for every request to avoid cross-request leakage.

### 2) Add an HTTP filter
Implement `OncePerRequestFilter`:
- read `X-Tenant-Id`
- validate UUID
- set `TenantContext`
- ensure `clear()` in `finally`

Suggested behavior:
- allow some paths to bypass (health, actuator) if needed.

### 3) Propagate tenant to logging context (optional)
Add `tenant_id` to MDC so logs can be filtered per tenant.
- set MDC value in filter
- remove it in finally

## Verification
- Call any endpoint with a valid `X-Tenant-Id` → request proceeds.
- Call without header → request is rejected.

## Common pitfalls
- Not clearing ThreadLocal.
- Allowing fallback to a default tenant for all requests (creates data leaks risk). If you need backward compatibility, make it temporary and explicit.
