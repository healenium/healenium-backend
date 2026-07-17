# Integration tests

Parent: [architecture.md](./architecture.md). Related: [db-rls.md](./db-rls.md), [app-set-local.md](./app-set-local.md).

## Goal

On **Pro**, continuously prove:

- A cannot read/modify B;  
- missing tenant context → no access;  
- RLS still scopes `findAll`-style queries.

## Tooling

- Testcontainers PostgreSQL  
- `@ActiveProfiles("pro")`  
- Liquibase `changelog-pro.xml` (RLS)  
- Runtime user without `BYPASSRLS`

## Seeding tenants

Insert ACTIVE rows in local `tenants`. Prefer **local DB** over live healenium-ai. Stub legacy AI client if still on the classpath until Phase 0 completes.

## Scenarios

1. **Read isolation** — seed A/B data; query under A → only A; same for B.  
2. **Write isolation** — under A, mutate B by id → 0 rows / exception.  
3. **Missing context** — empty reads; writes fail; `ProTenantTx` fails fast if null.  
4. **Broad query** — `findAll` under A still only A.  
5. **Optional HTTP** — missing tenant header `400`; unknown UUID `403`; ACTIVE proceeds (tests that hit HTTP must send M2M credential).

Set tenant via HTTP+filter or test helper (`TenantContext` + `TenantTxFacade`).

## Pass criteria

Fails if RLS not applied or `SET LOCAL` hook removed. No hard dependency on AI network for isolation proofs.
