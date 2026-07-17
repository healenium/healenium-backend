# Database schema: `tenants` + `tenant_id`

Parent: [architecture.md](./architecture.md).

## Goal

Introduce the tenant dimension in the Pro data plane:

- local `tenants` table (ACTIVE registry for API checks);
- `tenant_id` on every tenant-owned table;
- backfill into a **default tenant**;
- `tenant_id` NOT NULL;
- tenant-scoped indexes and uniqueness.

## Liquibase placement

| File | Role |
|------|------|
| `changelog-core.xml` | 001–010 shared tables |
| `016_free_tenant_id_compat.xml` | Free only — `tenant_id` columns (no registry) |
| `011_multitenancy_base.xml` | Pro — `tenants` + columns (skipped if Free already added columns) |
| `changelog-multitenancy.xml` | Pro chain: 011, 013, 014, 012, 015, 017 |
| `changelog-main.xml` | Free entrypoint |
| `changelog-pro.xml` | Pro entrypoint |

See [free-to-pro-upgrade.md](./free-to-pro-upgrade.md).

## Design

| Topic | Decision |
|-------|----------|
| Who creates tenants in production? | **healenium-ai**, then **sync** into this table |
| Who checks ACTIVE on API requests? | **Backend** against **local** `tenants` |
| Default tenant | `00000000-0000-0000-0000-000000000001` |
| Edition | Pro: registry + RLS; Free: `tenant_id` columns only (default UUID, no registry/RLS) |

Backend must not invent production tenants ad hoc.

## Preconditions

- PostgreSQL + Liquibase
- Schema: `SPRING_POSTGRES_SCHEMA` (default `healenium`)

## Steps

### 1. Default tenant UUID

Fixed constant for deterministic migrations.

### 2. Changelog

| Edition | Entrypoint |
|---------|------------|
| Free | `changelog-main.xml` → core + `016` (`tenant_id` columns only) |
| Pro | `changelog-pro.xml` → core + `changelog-multitenancy.xml` |

`011_multitenancy_base.xml` is Pro-only (registry + columns; column steps skip if Free already applied `016`).

### 3. Table `tenants`

| Column | Notes |
|--------|--------|
| `id` uuid PK | Same UUID as in AI after sync |
| `name` NOT NULL | |
| `status` NOT NULL default `ACTIVE` | |
| `created_at` NOT NULL | |

Insert default tenant in migration. Further rows via AI sync.

### 4. `tenant_id` on data tables

`selector`, `healing`, `healing_result`, `report`, `vcs`, `llm`:

1. Add nullable column  
2. Backfill default tenant  
3. NOT NULL  

Optional FK to `tenants(id)` (membership already FKs there).

### 5. Indexes

```sql
CREATE INDEX <table>_tenant_id_idx ON <table>(tenant_id);
```

Plus query-shaped composites, e.g. `(tenant_id, session_id)`.

### 6. Uniqueness

`UNIQUE(name)` → `UNIQUE(tenant_id, name)` (and similar).

## Verification

- NOT NULL `tenant_id` on owned tables; backfill correct  
- Indexes/uniques applied  
- `ddl-auto=validate` starts  
- Filter can resolve ACTIVE after synced rows exist  

## Rollback

Destructive once multi-tenant data exists; test in staging first.
