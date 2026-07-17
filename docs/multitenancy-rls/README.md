# Multi-tenancy documentation

Shared-schema multi-tenancy for **healenium-backend** (Pro) with PostgreSQL **RLS**.

## Start here

1. **[architecture.md](./architecture.md)** — approach: Free vs Pro, planes, runtime design  
2. **[implementation-plan.md](./implementation-plan.md)** — phased delivery plan across backend, AI, proxy, UI, infra  

## Detail guides

| Document | What it covers |
|----------|----------------|
| [free-to-pro-upgrade.md](./free-to-pro-upgrade.md) | Liquibase entrypoints and Free → Pro DB upgrade |
| [db-schema.md](./db-schema.md) | Local `tenants` registry, `tenant_id` on tables, Liquibase, default tenant |
| [db-rls.md](./db-rls.md) | Enable/FORCE RLS, policies, Pro-only changelog |
| [db-roles.md](./db-roles.md) | Runtime `healenium_app`, admin, Liquibase roles |
| [app-tenant-filter.md](./app-tenant-filter.md) | `Healenium-Tenant-Id`, local ACTIVE check, skip paths, Free vs Pro |
| [app-set-local.md](./app-set-local.md) | `SET LOCAL app.tenant_id` via `ProTenantTx` / `FreeTenantTx` |
| [app-entities.md](./app-entities.md) | JPA `tenantId`, entity listener, `TenantEdition` |
| [backend-contract.md](./backend-contract.md) | Caller headers, M2M, operational contract |
| [tests.md](./tests.md) | Integration tests for isolation |

## Reading order

1. [architecture.md](./architecture.md)  
2. [implementation-plan.md](./implementation-plan.md)  
3. [backend-contract.md](./backend-contract.md) if you integrate callers  
4. DB guides (`db-*`) then app guides (`app-*`) then [tests.md](./tests.md)

## Out of scope here

- Cross-tenant admin HTTP API (use privileged DB client)  
- JWT on healenium-backend (hlm-proxy)  
- Historical JWT-on-backend notes in `docs/tenant-membership/` (superseded)
