# 06 — Entity changes: persist `tenant_id`

## Goal
Ensure every INSERT/UPDATE respects RLS `WITH CHECK` by always writing the correct `tenant_id`.

## Required changes

### 1) Add `tenant_id` mapping to every tenant-owned JPA entity
For each entity backed by a tenant-owned table:
- add `tenantId` field
- map it:
  - `@Column(name = "tenant_id", nullable = false)`

Recommended:
- `updatable = false` to prevent tenant changes after creation.

### 2) Set tenantId on creation
Options:

**Option A — Service layer sets tenantId**
- In each create method:
  - `entity.setTenantId(TenantContext.getTenantId())`

**Option B — Entity listener (`@PrePersist`)** (recommended for consistency)
- Create a listener that:
  - reads tenant from `TenantContext`
  - sets `tenantId` if null
- Attach listener to all tenant-aware entities.

### 3) Prevent cross-tenant reassignment
- Mark `tenantId` as not updatable.
- Avoid exposing `tenantId` in public DTOs.

## Verification
- Create a record with header `X-Tenant-Id=A`:
  - row is inserted with `tenant_id=A`
- Attempt to insert with mismatched tenant_id:
  - rejected by RLS policy.

## Common pitfalls
- Forgetting to set tenantId in code for new entity types.
- Allowing client input to override tenantId.
