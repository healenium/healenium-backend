# Database roles and privileges

Parent: [architecture.md](./architecture.md). Related: [db-rls.md](./db-rls.md), [backend-contract.md](./backend-contract.md).

## Goal

Pro app cannot bypass RLS; ops can still inspect all data via a privileged DB client. Free does not depend on RLS session vars.

## Roles

| Role | Purpose |
|------|---------|
| **`healenium_app`** | Runtime datasource — no superuser, no `BYPASSRLS` |
| **`healenium_admin`** (or similar) | DBA client — `BYPASSRLS` or superuser |
| **Liquibase user** | DDL / policies — separate from hot runtime |

`application-pro.yml` expects `healenium_app` for runtime. No second Spring “super-admin” datasource in this design.

## Runtime grants (sketch)

```sql
CREATE ROLE healenium_app LOGIN PASSWORD '***';
GRANT USAGE ON SCHEMA healenium TO healenium_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA healenium TO healenium_app;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA healenium TO healenium_app;
-- defaults for future tables/sequences similarly
```

Runtime needs **SELECT on `tenants`** for local ACTIVE validation.

## Admin (sketch)

```sql
CREATE ROLE healenium_admin LOGIN PASSWORD '***';
ALTER ROLE healenium_admin BYPASSRLS;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA healenium TO healenium_admin;
-- + schema / sequences as needed
```

## App config

- Runtime: `SPRING_POSTGRES_USER=healenium_app`  
- Liquibase: separate `SPRING_LIQUIBASE_USER` as in `application-pro.yml`  
- Never run the app as admin

## Verification

| Connect as | Without `SET LOCAL` | With `SET LOCAL` |
|------------|---------------------|------------------|
| `healenium_app` | 0 rows on tenant tables | Tenant rows |
| admin | All rows | — |

## Notes

Migrations need DDL rights; keep them off the request path. Correct filter + wrong role still breaks isolation.
