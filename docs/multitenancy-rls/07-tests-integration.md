# 07 — Integration tests for RLS isolation

## Goal
Prove (and continuously verify) that:
- tenant A cannot read/modify tenant B data;
- missing tenant context results in no access;
- RLS protects against developer mistakes (missing tenant filters).

## Recommended tooling
- Testcontainers PostgreSQL
- Spring Boot integration tests
- Liquibase migrations applied to the container DB

## Test scenarios

### 1) Read isolation
1. Create tenant A and tenant B (insert into `tenants`).
2. Insert test rows for both tenants.
3. Execute repository queries under tenant A context:
   - verify only A rows are returned.
4. Repeat for tenant B.

### 2) Write isolation
Under tenant A:
- attempt to update/delete a tenant B row by ID:
  - should affect 0 rows or throw an exception (depending on JPA behavior)

### 3) Missing tenant context
- call repository without setting `app.tenant_id`:
  - reads return empty
  - writes fail due to `WITH CHECK`

### 4) Guard against missing WHERE clause
- call a broad query like `findAll()`:
  - verify it still returns only current tenant rows
  - this proves RLS is working as a safety net.

## Implementation notes
- In tests you can set tenant context either by:
  - invoking the same code path as production (HTTP + filter), or
  - directly calling the mechanism that performs `SET LOCAL app.tenant_id`.

## Pass criteria
- All scenarios pass reliably.
- Tests fail if RLS is disabled or if `SET LOCAL` hook is removed.
