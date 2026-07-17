# Free → Pro database upgrade

Liquibase entrypoints after Phase B:

| Edition | Changelog | Contents |
|---------|-----------|----------|
| **Free** | `classpath:/db/changelog/changelog-main.xml` | `changelog-core` (001–010) + `016_free_tenant_id_compat` (`tenant_id` columns only) |
| **Pro** | `classpath:/db/changelog/changelog-pro.xml` | `changelog-core` + `changelog-multitenancy` (`011` tenants registry, `013`/`014` membership, `012`/`015` RLS) |

Shared JPA entities require a `tenant_id` column. Free gets **columns only** (always default UUID `00000000-0000-0000-0000-000000000001`). Free does **not** get `tenants` / `membership` / RLS.

---

## Upgrade steps (Free install → Pro)

1. **Backup** the database.
2. Point the app at Pro config:
   - `spring.profiles.active=pro` (or your Pro profile group)
   - `spring.liquibase.change-log=classpath:/db/changelog/changelog-pro.xml`
   - Runtime DB user `healenium_app` without `BYPASSRLS` ([db-roles.md](./db-roles.md))
3. Start the app (or run Liquibase once). Pro applies, in order:
   - Skip core changes already applied
   - `011`: create `tenants` + default row; **skip** `tenant_id` column changesets if Free already ran `016` (preConditions)
   - `013` / `014`: membership (+ optional `dev` seed)
   - `012` / `015`: RLS policies
   - `017`: membership unique allows multi-tenant identities
4. **Seed / sync tenants** for real customers via healenium-ai → `PUT /internal/tenants` or `POST /tenants/sync` (default row alone is not enough for multi-tenant SaaS).
5. **Grant UI membership** via `PUT /internal/membership` (`issuer` + `sub` + `tenantId`).
6. Set M2M secrets in production (`M2M_INTERNAL_TOKEN`; `M2M_API_KEYS` for Mobitru/direct). Pro always enforces M2M.
7. Smoke-test: request with M2M credential + `Healenium-Tenant-Id` of an ACTIVE tenant; cross-tenant isolation; Free regression elsewhere.

## Idempotency notes

- Do **not** drop leftover `tenants`/`membership` on old Free DBs that once ran full `011` while it lived under `changelog-main`. Harmless residue; Pro will `MARK_RAN` existing objects via preConditions where added.
- Fresh Free never creates `tenants` / `membership` / RLS.
- Fresh Pro never runs `016` (Pro entrypoint uses `changelog-core`, not Free `changelog-main`).

## Rollback

Rolling Pro → Free is **not** supported as an automated Liquibase rollback (RLS/policies/`tenants` stay). Restore from backup taken before the upgrade.
