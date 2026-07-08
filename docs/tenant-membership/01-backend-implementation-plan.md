# План имплементации: `membership` + JWT в healenium-backend (profile `pro`)

Цель: таблица **`membership`** (`issuer`, `external_sub`, `tenant_id` → FK `tenants`), резолв «кто из Cognito в каком tenant», **`GET /me`**, и доработка **`TenantFilter`**: при `Authorization: Bearer` — проверка membership; без Bearer — текущее поведение (WebDriver / API key только `X-Tenant-Id`). Данные по-прежнему в Postgres с RLS; **RLS на таблицу `membership` не включать** (таблица не tenant-owned).

Опорные файлы сейчас:

- Liquibase: [`src/main/resources/db/changelog/011_multitenancy_base.xml`](../../src/main/resources/db/changelog/011_multitenancy_base.xml) (`tenants`), [`changelog-pro.xml`](../../src/main/resources/db/changelog/changelog-pro.xml) (подключает `012_multitenancy_rls.xml`).
- Фильтр: [`TenantFilter.java`](../../src/main/java/com/epam/healenium/tenant/TenantFilter.java), регистрация [`TenantConfiguration.java`](../../src/main/java/com/epam/healenium/tenant/TenantConfiguration.java) (`@Profile("pro")`, order `Integer.MIN_VALUE`).
- Валидация tenant в AI: [`TenantValidationService.java`](../../src/main/java/com/epam/healenium/tenant/ai/TenantValidationService.java) (Caffeine уже есть в `build.gradle`).

---

## Шаг 1 — Зависимости (JWT)

> **Статус:** из-за plan mode автоправки кода недоступны — примените вручную или включите agent mode.

### 1a. `build.gradle`

После строки `spring-boot-starter-validation` добавьте:

```gradle
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

### 1b. `application.yml` (только профиль `pro`)

В блок `---` / `on-profile: pro` (после секции `liquibase` или рядом с `healenium:`) добавьте:

```yaml
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${COGNITO_ISSUER_URI}
```

Переменная окружения **`COGNITO_ISSUER_URI`** — issuer Cognito User Pool, например  
`https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_xxxxxxxxx`.

Пока **нет** `SecurityFilterChain` с OAuth2 (шаг 5), только зависимость и свойство **не включают** проверку JWT на запросах. После добавления security включённый `issuer-uri` заставит Spring резолвить JWKS при старте — до шага 5 можно **не** добавлять блок `security` в YAML, а ограничиться только пунктом **1a**; блок **1b** перенести на шаг 5 вместе с конфигом.

**Минимальный шаг 1 сейчас:** только **1a** (`oauth2-resource-server` в Gradle), затем `./gradlew compileJava`.

---

## Шаг 2 — Liquibase: таблица `membership`

1. Новый файл, например **`src/main/resources/db/changelog/013_membership.xml`**.
2. Подключить его **только в** [`changelog-pro.xml`](../../src/main/resources/db/changelog/changelog-pro.xml) **после** `012_multitenancy_rls.xml` (таблица относится к SaaS Pro, не к базовому main-only).

Содержимое миграции (минимум):

- Таблица **`membership`**: `issuer` TEXT NOT NULL, `external_sub` TEXT NOT NULL, `tenant_id` UUID NOT NULL, опционально `role` VARCHAR, `created_at` TIMESTAMPTZ DEFAULT now().
- **FK** `tenant_id` → `tenants(id)`.
- Индекс по `(issuer, external_sub)` для выборки.
- **UNIQUE(`issuer`, `external_sub`)** для MVP «один логин — один tenant».
- **Не** включать для `membership` RLS из `012` (и не добавлять `tenant_id` политику на эту таблицу).

---

## Шаг 3 — JPA: сущность и репозиторий

- Пакет, например `com.epam.healenium.tenant.membership`.
- **`Membership`** (`@Entity`, `@Table(name = "membership")`) с полями как в Liquibase; составной уникальный индекс согласовать с `@Table` / `@UniqueConstraint`.
- **`MembershipRepository`**: `List<Membership> findByIssuerAndExternalSub(String issuer, String externalSub);` (или проекция только `tenant_id`).

При необходимости read-only сущность **`Tenant`** для FK — опционально, если не хотите «голый» UUID без связи.

---

## Шаг 4 — Сервис резолва + кэш

- **`MembershipResolutionService`** (или `MembershipService`): по `(issuer, sub)` → `List<UUID> tenantIds`.
- Кэш **Caffeine** по ключу `(issuer, sub)` по аналогии с [`TenantValidationService`](../../src/main/java/com/epam/healenium/tenant/ai/TenantValidationService.java) (TTL и max size из `@Value`).

---

## Шаг 5 — Spring Security (только `pro`)

1. **`SecurityConfiguration`** `@Profile("pro")`: `SecurityFilterChain` с `oauth2ResourceServer().jwt()`, matcher: разрешить без auth **`/actuator/**`**, **`/swagger-ui/**`**, **`/v3/api-docs/**`** (согласовать с [`TenantFilter.SKIP_PREFIXES`](../../src/main/java/com/epam/healenium/tenant/TenantFilter.java)); остальное — **authenticated** или явный список путей API.
2. Убедиться, что **JWT-фильтры Spring Security выполняются до** логики tenant: сейчас `TenantFilter` зарегистрирован с **`Integer.MIN_VALUE`** — он может отработать **раньше** `BearerTokenAuthenticationFilter`. **Исправить порядок**: например выставить `FilterRegistrationBean` order **после** security (например `Ordered.LOWEST_PRECEDENCE - 100`) или перенести установку tenant в **отдельный фильтр** внутри цепочки Security **после** аутентификации JWT.

Критерий: в момент проверки `X-Tenant-Id` в кастомном коде уже есть **`Jwt` в `SecurityContextHolder`**, если в запросе был Bearer.

---

## Шаг 6 — Доработка `TenantFilter` (или выделенный `TenantMembershipFilter`)

Логика для **`pro`**:

1. Если в запросе есть валидный **JWT** (наличие аутентификации `Jwt` в контексте **или** заголовок `Authorization: Bearer` + доверенный парсинг — предпочтительно только контекст после шага 5):
   - Взять **`iss`**, **`sub`** из `Jwt`.
   - Загрузить разрешённые `tenant_id` через `MembershipResolutionService`.
   - Если список пуст → **403**.
   - Если **один** tenant: **подставить** его в качестве эффективного tenant (игнорировать или сверять клиентский `X-Tenant-Id`).
   - Если **несколько** (после снятия UNIQUE в будущем): требовать `X-Tenant-Id` ∈ списка, иначе **403**.
2. Если **нет** JWT (WebDriver и т.д.): оставить текущую логику — только **`X-Tenant-Id`** + **`TenantValidationService.isTenantAllowed`** (healenium-ai).

Путь **`/me`**: не требовать `X-Tenant-Id` до резолва membership — добавить путь в **`shouldNotFilter`** или отдельный контроллер вне общего tenant-guard (см. шаг 7).

---

## Шаг 7 — `GET /me`

- Контроллер, например **`MeController`** `@Profile("pro")`, путь согласовать с префиксом API (например **`/api/me`** или **`/me`** — единообразно с UI).
- Из **`Jwt`** прочитать `iss`, `sub`, вернуть JSON: `{ "tenants": [...], "defaultTenantId": "..." }`.
- Этот endpoint должен быть **доступен с Bearer без обязательного `X-Tenant-Id`** (иначе `TenantFilter` отрежет запрос). Варианты: исключение в `TenantFilter` для `GET /me`, или вынести `/me` в цепочку без tenant-фильтра.

---

## Шаг 8 — Конфигурация и секреты

- Issuer / optional audience в `application-pro.yml` (или env).
- Документировать для деплоя: переменные для Cognito pool.

---

## Шаг 9 — Данные для разработки

- SQL/Liquibase **dev** или ручная вставка: одна строка в **`membership`** для тестового `issuer`+`sub` и **`tenant_id`** = дефолтный tenant из `011` (`00000000-0000-0000-0000-000000000001`), чтобы локальный UI с Cognito сразу проходил фильтр.

---

## Шаг 10 — Тесты

- **Testcontainers** + поднять контекст с `pro` (или slice): JWT можно подменить **mock** `SecurityContext` + тест репозитория `membership`.
- Интеграционный тест: запрос с заголовками **без** membership → 403; с membership и корректным `X-Tenant-Id` → 200; без Bearer, только `X-Tenant-Id` → как сейчас (если tenant ACTIVE в AI mock).

---

## Порядок выполнения (краткий чеклист)

| № | Действие | Статус |
|---|----------|--------|
| 1 | `build.gradle`: oauth2-resource-server (+ опционально YAML `issuer-uri` вместе с шагом 5) | Вручную / agent |
| 2 | `013_membership.xml` + include в `changelog-pro.xml` | |
| 3 | Entity + `MembershipRepository` | |
| 4 | `MembershipResolutionService` + Caffeine | |
| 5 | `SecurityConfiguration` @Profile("pro"), issuer-uri | |
| 6 | Порядок фильтров: JWT до tenant | |
| 7 | `TenantFilter` / новый фильтр: ветка Bearer vs нет | |
| 8 | `GET /me` + исключение из обязательного `X-Tenant-Id` | |
| 9 | `application-pro.yml` + пример dev-строки `membership` | |
|10 | Тесты | |

После этого — отдельно UI (hlm-ui-dashboard) и прокси (проброс Bearer уже есть по архитектуре).

---

## Риски

- **Порядок фильтров** — главный технический риск; проверить логами один запрос с Bearer.
- **Два профиля** (`free` без JWT) — не включать resource server глобально без `@Profile("pro")`.
- Имя таблицы **`membership`** при конфликте с ORM — квалифицировать или переименовать в `hlm_membership` (см. общий план в Cursor).
