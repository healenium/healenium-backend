# 05 — Application transaction hook: `SET LOCAL app.tenant_id`

## Goal
Before executing any SQL within a transaction, set the PostgreSQL session parameter used by RLS:

```sql
SET LOCAL app.tenant_id = '<tenant-uuid>';
```

This must happen for every transactional unit of work that touches the database.

## Why `SET LOCAL`
- Applies only to the current transaction.
- Safe with connection pools (no tenant leakage between requests).

## Implementation approach (Spring)

### 1) Ensure tenant is resolved before DB access
The HTTP filter (see `04-app-tenant-context.md`) must run before any service code.

### 2) Execute `SET LOCAL` after transaction start
The critical detail: `SET LOCAL` should be executed when a DB transaction is already active.

Practical options:
- AOP around `@Transactional` methods: call `JdbcTemplate.execute("SET LOCAL ...")` at method start.
- Transaction synchronization: register a callback that runs at transaction start.

### 3) Fail fast when tenant is missing
If `TenantContext.getTenantId()` is null:
- throw an exception mapped to `400/403`
- do not call the database

### 4) Keep it centralized
Do not rely on developers remembering to call `SET LOCAL`.
Implement it once in an infrastructure layer.

## Verification
- With RLS enabled, call an endpoint:
  - if `SET LOCAL` is not executed: repositories return 0 results and/or writes fail.
  - if executed: data is tenant-scoped as expected.

## Common pitfalls
- Executing `SET LOCAL` outside of a transaction (no effect).
- Using `SET app.tenant_id = ...` (non-local) with Hikari pooling (leaks between requests).
