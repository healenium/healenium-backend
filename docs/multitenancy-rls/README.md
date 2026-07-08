# Multi-tenancy (shared schema) with PostgreSQL RLS

This folder contains step-by-step implementation guides to introduce multi-tenancy in **healenium-backend** using a shared schema approach (single DB, single schema) with strong isolation enforced by **PostgreSQL Row Level Security (RLS)**.

## Scope
- Shared schema + `tenant_id` column in all tenant-owned tables
- PostgreSQL RLS policies enforcing tenant isolation
- Application-side `TenantContext` and `SET LOCAL app.tenant_id` per transaction
- `tenant_id` propagation on inserts/updates
- Integration tests verifying isolation

## Non-scope
- Admin endpoints / second datasource for super-admin. Super-admin access is expected via a DB client using a privileged DB role.

## Documents
1. [01 Database schema changes (tenants + tenant_id)](./01-db-schema-tenant-id.md)
2. [02 PostgreSQL RLS setup](./02-db-rls-policies.md)
3. [03 Database roles & privileges](./03-db-roles-and-privileges.md)
4. [04 Application tenant context (HTTP)](./04-app-tenant-context.md)
5. [05 Application transaction hook: `SET LOCAL app.tenant_id`](./05-app-set-local-tenant.md)
6. [06 Entity changes: persist `tenant_id`](./06-app-entity-tenant-id.md)
7. [07 Integration tests](./07-tests-integration.md)
