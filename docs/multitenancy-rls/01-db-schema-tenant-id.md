# 01 — Database schema changes: `tenants` + `tenant_id`

## Goal
Introduce a **tenant dimension** into the database schema by:
- creating a `tenants` table;
- adding a `tenant_id` column to every tenant-owned table;
- backfilling existing data into a **default tenant**;
- making `tenant_id` **NOT NULL**;
- ensuring indexes/uniqueness are tenant-scoped.

## Preconditions
- Project uses PostgreSQL and Liquibase (`src/main/resources/db/changelog/*`).
- Current schema name is controlled by `SPRING_POSTGRES_SCHEMA` (defaults to `healenium`).

## Step-by-step

### 1) Choose and fix a Default Tenant UUID
Pick a constant UUID that will represent existing single-tenant data.

Example:
- `DEFAULT_TENANT_ID = 00000000-0000-0000-0000-000000000001`

Why constant: makes migrations deterministic and repeatable.

### 2) Add a new Liquibase changelog file
Create a new changelog, e.g.:
- `src/main/resources/db/changelog/011_multitenancy_base.xml`

And include it in `changelog-main.xml` after existing table creation changelogs.

### 3) Create the `tenants` table
In the new changelog:
- create table `tenants`
  - `id uuid primary key`
  - `name text not null`
  - `status text not null default 'ACTIVE'`
  - `created_at timestamptz not null default now()`

Insert the default tenant row with the fixed UUID.

### 4) Add `tenant_id` to tenant-owned tables
For each tenant-owned table (currently: `selector`, `healing`, `healing_result`, `report`, `vcs`, `llm`):

1. Add a nullable column:
   - `ALTER TABLE <table> ADD COLUMN tenant_id uuid;`
2. Backfill existing rows:
   - `UPDATE <table> SET tenant_id = '<DEFAULT_TENANT_ID>' WHERE tenant_id IS NULL;`
3. Enforce not null:
   - `ALTER TABLE <table> ALTER COLUMN tenant_id SET NOT NULL;`

### 5) Add tenant-scoped indexes
Add at least:
- `CREATE INDEX <table>_tenant_id_idx ON <table>(tenant_id);`

Additionally, add composite indexes that match real queries (examples):
- `(tenant_id, session_id)`
- `(tenant_id, created_at)`
- `(tenant_id, report_name)`

### 6) Make unique constraints tenant-scoped
If a table currently has a global uniqueness constraint, change it to include `tenant_id`.

Examples:
- change `UNIQUE(name)` → `UNIQUE(tenant_id, name)`
- change `UNIQUE(url)` → `UNIQUE(tenant_id, url)`

This prevents cross-tenant collisions.

## Verification
After applying migrations:
1. Every tenant-owned table contains `tenant_id` and it is NOT NULL.
2. Existing data has `tenant_id = DEFAULT_TENANT_ID`.
3. Indexes exist.
4. App still starts with `ddl-auto=validate`.

## Rollback notes
- Rollback requires dropping `tenant_id` columns and `tenants` table, which is destructive if new tenants exist.
- Prefer testing in staging first.
