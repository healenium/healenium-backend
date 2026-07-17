# Application: persist `tenant_id` on entities

Parent: [architecture.md](./architecture.md). Related: [db-schema.md](./db-schema.md), [app-tenant-filter.md](./app-tenant-filter.md).

## Goal

INSERT/UPDATE must write the correct `tenant_id` so RLS `WITH CHECK` passes.

## Mapping

On tenant-owned entities (`Selector`, `Healing`, `HealingResult`, `Report`, `Vcs`, `Llm`, …):

- `tenantId` → `@Column(name = "tenant_id", nullable = false)`  
- Prefer `updatable = false`

## Set on create

**Recommended:** `TenantEntityListener` (`@PrePersist`) + `TenantEdition`:

| Edition | Behaviour |
|---------|-----------|
| **Pro** | Require `TenantContext`; set; fail if missing |
| **Free** | Default tenant `00000000-0000-0000-0000-000000000001` |

Do not take `tenantId` from public DTOs. Clients send `Healenium-Tenant-Id` only at the edge/proxy.

## Prevent reassignment

Non-updatable column; no API to change tenant. RLS still rejects row `tenant_id` ≠ `app.tenant_id`.

## Verification

Pro create with `Healenium-Tenant-Id=A` → row A. Mismatch vs session → RLS reject. Free creates land on default without header.

## Pitfalls

New entity without listener; client-controlled body field; Pro create outside request scope without context.
