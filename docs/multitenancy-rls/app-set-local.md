# Application: `SET LOCAL app.tenant_id`

Parent: [architecture.md](./architecture.md). Related: [db-rls.md](./db-rls.md), [app-tenant-filter.md](./app-tenant-filter.md).

## Goal

On **Pro**, before SQL against tenant-owned tables, set:

```sql
SET LOCAL app.tenant_id = '<tenant-uuid>';
```

## Edition

| Profile | Facade | Behaviour |
|---------|--------|-----------|
| `pro` | `ProTenantTx` | Inside `@Transactional`, `SET LOCAL` from `TenantContext`, then work |
| `free` | `FreeTenantTx` | Pass-through; no session var |

Use `TenantTxFacade` at call sites so Free/Pro stay consistent.

## Why `SET LOCAL`

Transaction-scoped; safe with Hikari. Never non-local `SET app.tenant_id` on pooled connections.

## Implementation

1. Filter sets `TenantContext` before services open DB work.  
2. `SET LOCAL` must run **after** transaction start (`ProTenantTx.required` + `JdbcTemplate`, or AOP / txn sync).  
3. If context null in Pro → fail fast; do not query tenant tables.  
4. Centralize — do not rely on developers remembering the SQL.

## Verification

With RLS + `healenium_app`: without `ProTenantTx` → empty/failed; with it → tenant-scoped. Free works without header/session var.

## Pitfalls

Outside transaction; non-local SET; bypassing facade in Pro; assuming Free needs the same hook.
