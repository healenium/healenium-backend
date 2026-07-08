# 02 — PostgreSQL RLS setup (tenant isolation)

## Goal
Enforce tenant isolation at the **database layer** using PostgreSQL **Row Level Security (RLS)**.

With RLS enabled, the database will only return/modify rows whose `tenant_id` matches the current request tenant, set via:

```sql
SET LOCAL app.tenant_id = '<tenant-uuid>';
```

## Key design
- Each tenant-owned table has `tenant_id uuid NOT NULL`.
- RLS policy checks `tenant_id` against `current_setting('app.tenant_id', true)`.
- Access without `app.tenant_id` is denied by default (no rows visible, writes blocked).

## Step-by-step

### 1) Add a dedicated Liquibase changelog
Create:
- `src/main/resources/db/changelog/012_multitenancy_rls.xml`

Include it in `changelog-main.xml` after `tenant_id` is present and NOT NULL.

### 2) Enable RLS on each tenant-owned table
For each table (`selector`, `healing`, `healing_result`, `report`, `vcs`, `llm`):

```sql
ALTER TABLE <table> ENABLE ROW LEVEL SECURITY;
```

### 3) Force RLS (recommended)
Force RLS so it applies even to the table owner:

```sql
ALTER TABLE <table> FORCE ROW LEVEL SECURITY;
```

Notes:
- PostgreSQL superusers can still bypass RLS. Do not run the application with a superuser DB role.

### 4) Create tenant isolation policy
Create one policy per table:

```sql
CREATE POLICY <table>_tenant_isolation
ON <table>
USING (
  tenant_id = current_setting('app.tenant_id', true)::uuid
)
WITH CHECK (
  tenant_id = current_setting('app.tenant_id', true)::uuid
);
```

Why `current_setting(..., true)`:
- returns `NULL` instead of throwing if not set;
- `tenant_id = NULL` evaluates to NULL/false → no rows match;
- writes fail because `WITH CHECK` won’t pass.

### 5) (Optional) Add explicit “deny if unset” guard
If you prefer explicitness, you can add an extra policy or rewrite the policy to ensure `app.tenant_id` is set.
However, the equality check against NULL already behaves as deny-by-default.

## Operational notes
- `SET LOCAL` works **only inside a transaction**. Ensure the app sets it after transaction start.
- If you use connection pooling (Hikari), do NOT use `SET app.tenant_id = ...` without LOCAL, otherwise values may leak between requests.

## Verification (manual SQL)
Assuming you have a test table `selector` with two tenants `A` and `B`:

1) Without tenant set:
```sql
BEGIN;
SELECT * FROM selector; -- should return 0 rows
ROLLBACK;
```

2) With tenant A:
```sql
BEGIN;
SET LOCAL app.tenant_id = '<A>';
SELECT * FROM selector; -- should return only tenant A rows
ROLLBACK;
```

3) Write protection:
```sql
BEGIN;
SET LOCAL app.tenant_id = '<A>';
INSERT INTO selector(..., tenant_id) VALUES (..., '<B>'); -- should fail
ROLLBACK;
```

## Common pitfalls
- App DB role is superuser or has `BYPASSRLS`.
- `SET LOCAL` executed outside transaction.
- `tenant_id` not populated on inserts.
