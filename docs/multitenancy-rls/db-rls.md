# PostgreSQL RLS (tenant isolation)

Parent: [architecture.md](./architecture.md). Related: [db-roles.md](./db-roles.md), [app-set-local.md](./app-set-local.md).

## Goal

Enforce isolation at the DB layer on **Pro**. After the app sets the request tenant:

```sql
SET LOCAL app.tenant_id = '<tenant-uuid>';
```

Application wiring: `ProTenantTx`. Free (`FreeTenantTx`) does **not** set this and does **not** rely on RLS.

## Design

- Tenant-owned tables have `tenant_id uuid NOT NULL` ([db-schema.md](./db-schema.md)).
- Policy: `tenant_id = current_setting('app.tenant_id', true)::uuid`.
- Unset setting → no rows / failed writes.
- RLS is defence in depth — not a substitute for ACTIVE checks or proxy/M2M auth.

## Steps

### 1. Changelog (Pro-only)

`012_multitenancy_rls.xml` — include from **`changelog-pro.xml` only**, after `tenant_id` is NOT NULL.

### 2. Enable + FORCE RLS

Per table (`selector`, `healing`, `healing_result`, `report`, `vcs`, `llm`):

```sql
ALTER TABLE <table> ENABLE ROW LEVEL SECURITY;
ALTER TABLE <table> FORCE ROW LEVEL SECURITY;
```

Runtime must not be superuser / `BYPASSRLS` ([db-roles.md](./db-roles.md)).

### 3. Policy

```sql
CREATE POLICY <table>_tenant_isolation
ON <table>
USING (tenant_id = current_setting('app.tenant_id', true)::uuid)
WITH CHECK (tenant_id = current_setting('app.tenant_id', true)::uuid);
```

`current_setting(..., true)` returns NULL if unset → deny-by-default.

### 4. No RLS on

- `tenants` — registry  
- `membership` — identity → tenant (not tenant-owned)

## Ops

- `SET LOCAL` only inside a transaction.  
- Never non-local `SET app.tenant_id` with Hikari (leak).

## Manual checks (as `healenium_app`)

Without `SET LOCAL` → 0 rows.  
With tenant A → only A.  
Insert with `tenant_id=B` under A → fail.

## Pitfalls

Wrong DB role; `SET LOCAL` outside txn; missing `tenant_id` on insert; applying `012` to Free.
