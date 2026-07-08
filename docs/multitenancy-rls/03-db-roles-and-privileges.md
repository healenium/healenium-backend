# 03 — Database roles & privileges (runtime vs admin)

## Goal
Ensure the application cannot bypass RLS, while still allowing a human admin to access all data via a DB client.

## Recommended roles
1) **Application runtime role** (used by the service):
- name example: `healenium_app`
- must NOT be superuser
- must NOT have `BYPASSRLS`

2) **Human admin role** (used by DBA/ops in DB client):
- name example: `healenium_admin`
- may be superuser, or may have `BYPASSRLS`

> You can keep your current `healenium_user` as runtime role, but validate it has no bypass privileges.

## Step-by-step

### 1) Check current role privileges
Run as DBA:

```sql
\du
```

Confirm runtime role is not:
- `Superuser`
- `Bypass RLS`

### 2) Create/adjust runtime role
Example (adapt to your environment):

```sql
CREATE ROLE healenium_app LOGIN PASSWORD '***';
```

Grant privileges to the schema and tables (replace schema name):

```sql
GRANT USAGE ON SCHEMA healenium TO healenium_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA healenium TO healenium_app;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA healenium TO healenium_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA healenium
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO healenium_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA healenium
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO healenium_app;
```

### 3) Create/adjust admin role (for DB client)
Option A (recommended): bypass RLS but not necessarily superuser:

```sql
CREATE ROLE healenium_admin LOGIN PASSWORD '***';
ALTER ROLE healenium_admin BYPASSRLS;
GRANT USAGE ON SCHEMA healenium TO healenium_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA healenium TO healenium_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA healenium TO healenium_admin;
```

Option B: use superuser (only if your ops model allows it).

### 4) Configure application to use only runtime role
Update deployment secrets/env:
- `SPRING_POSTGRES_USER=healenium_app`
- `SPRING_POSTGRES_PASSWORD=...`

Do not use the admin role in the application.

## Verification
1) Connect as runtime role and try:
- `SELECT * FROM <table>` without `SET LOCAL app.tenant_id` → returns 0 rows.
- With `SET LOCAL` → returns tenant rows.

2) Connect as admin role and try:
- `SELECT * FROM <table>` without `SET LOCAL` → returns all rows.

## Notes about Liquibase user
Ideally, Liquibase migrations run with a separate migration role that can run DDL. Runtime role should be DML-only.
If you currently run Liquibase with runtime role, keep it temporarily but plan to split later.
