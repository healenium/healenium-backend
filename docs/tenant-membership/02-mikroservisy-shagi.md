# Шаги в других микросервисах (после реализации в healenium-backend)

Ниже — что **ещё** нужно сделать по сервисам, чтобы связка **Cognito JWT + `membership` + `X-Tenant-Id` + healenium-ai** работала end-to-end. Порядок шагов внутри раздела можно считать рекомендуемым.

---

## 1. healenium-backend

1. В окружении **профиля `pro`** задать **`COGNITO_ISSUER_URI`** (issuer User Pool, например `https://cognito-idp.<region>.amazonaws.com/<pool_id>`), перезапустить сервис.
2. Убедиться, что токен с UI/WebDriver содержит **`iss` и `sub`**, которые совпадают с тем, что вы пишете в таблицу **`membership`** (для Cognito обычно `iss` = issuer pool, `sub` = стабильный идентификатор пользователя).
3. Заполнить **`membership`**: для каждого пользователя UI — строка `issuer` + `external_sub` + `tenant_id` (FK на `tenants`). Без строки пользователь с валидным JWT получит **403** на защищённых tenant-путях.
4. Проверить **`healenium.ai.base-url`**: backend должен достучаться до healenium-ai для **`GET /tenants/{id}`** (валидация «tenant активен»).
5. (Опционально) Если включите проверку **audience** в Spring Security — добавить соответствующую настройку JWT и выровнять её с тем, что выдаёт Cognito и прокси.

---

## 2. healenium-proxy (Spring Cloud Gateway)

1. Найти маршруты к **healenium-backend** (REST, не только Selenium): убедиться, что входящий **`Authorization: Bearer …`** **не удаляется** и **передаётся** в downstream (не подменяется на Basic, если это запрос от UI с Cognito).
2. Для тех же маршрутов убедиться, что **`X-Tenant-Id`**, присланный клиентом, **пробрасывается** на backend без перезаписи (если нет отдельной политики «только из доверенного источника»).
3. Для потока **WebDriver**: сохранить текущую модель — **`X-Tenant-Id`** из capability (например `hlm:api_key`) / контекста сессии уходит на backend; **Bearer** не обязателен.
4. Сверить **публичный URL** и TLS с тем, как UI строит запросы к API (чтобы не было расхожения host/path и обрезания заголовков на балансировщике).
5. Если перед gateway стоит **ALB + Cognito**: проверить, что до gateway доходит **оригинальный** Bearer (или согласованная схема подмены), и issuer в токене совпадает с **`COGNITO_ISSUER_URI`** на backend.

---

## 3. healenium-ai

1. Поддерживать контракт **`GET /tenants/{id}`**: **200** если tenant существует и активен, **404** если нет, **403/410** (или иной явный код) если отключён — backend **`TenantValidationService`** интерпретирует не-404 как «не активен».
2. Закрыть **создание/изменение** tenant (**`POST /tenants`**, прочие админские операции): только из внутренней сети, **API key**, **mTLS** или **OAuth2 client** — не оставлять публично без auth.
3. В Kubernetes/Docker: сетевые политики / security group так, чтобы **`POST /tenants`** был доступен только **healenium-backend** (или CI), а **`GET /tenants/{id}`** — backend’у и при необходимости прокси.
4. Согласовать **дефолтный tenant** и создание записей в AI с миграцией **`tenants`** в Postgres backend (один и тот же UUID tenant’а в обоих местах).

---

## 4. hlm-ui-dashboard

1. После логина **Cognito** получать токен, который backend может валидировать (**access** или **id** — в зависимости от настроек pool и resource server; выровнять с backend).
2. Добавить (или расширить) **axios interceptor** для запросов к **healenium-backend** (через ваш base URL / gateway): всегда отправлять **`Authorization: Bearer <token>`**.
3. При старте сессии (или после логина) вызвать **`GET /healenium/me`** с Bearer **без** обязательного `X-Tenant-Id`, получить **`tenants`** и **`defaultTenantId`**.
4. Реализовать **выбор tenant** (если `tenants.length > 1`): сохранять выбранный UUID и на каждый запрос к backend добавлять **`X-Tenant-Id: <uuid>`**. Если tenant один — можно подставлять `defaultTenantId` автоматически.
5. Не использовать «ручной» UUID tenant без сверки с ответом **`/me`**, иначе backend вернёт **403** (несовпадение с `membership`).
6. Оставить/настроить отдельно вызовы к **healenium-ai** (если идут с фронта): там по-прежнему могут быть **`X-Session-Id`** и т.д.; не смешивать с заголовками backend без необходимости.

---

## 5. Примеры клиентов (healenium-example-maven, healenium-example-playwright-javascript и т.п.)

1. Для сценариев **через healenium-proxy к backend**: передавать **`X-Tenant-Id`** (UUID tenant из healenium-ai / `tenants`), **без** Bearer, если тестируете только WebDriver-поток.
2. В capability / настройках драйвера использовать тот же механизм, что и в прокси (**`hlm:api_key`** или ваш актуальный ключ), чтобы прокси выставил **`X-Tenant-Id`** к backend.
3. Если нужно тестировать **UI-поток с JWT**: добавить в пример передачу **`Authorization`** и **`X-Tenant-Id`** по правилам из раздела 4 (вне scope обычного «только Selenium»).

---

## 6. Kubernetes / Helm / docker-compose (инфраструктура)

1. В манифестах **healenium-backend (pro)** добавить секрет/env **`COGNITO_ISSUER_URI`** (и при необходимости переменные для audience).
2. Убедиться, что **healenium-backend** резолвит **healenium-ai** по DNS/сервисному имени и портам, как в **`HEALENIUM_AI_SERVICE`** / `healenium.ai.base-url`.
3. Для **healenium-ai** не публиковать наружу эндпоинты создания tenant без защиты (см. раздел 3).
4. При первом деплое: однажды выполнить **Liquibase** до версии с **`membership`**, затем вставить строки **`membership`** (или автоматизировать через ваш provisioning).

---

## Backend (уже в репозитории healenium-backend)

1. **`application-pro.yml`** — конфигурация только для **`pro`**: datasource, liquibase `changelog-pro.xml`, **`spring.security.oauth2.resourceserver.jwt.issuer-uri`**, блок **`healenium`** (AI URL, кэш tenant/membership).
2. Локальный сид **`membership`**: профиль **`devlocal`** (в `application.yml` группа **`devlocal` → `pro`**) + **`application-devlocal.yml`** задаёт **`spring.liquibase.contexts=dev`** и параметры **`membershipSeedIssuer` / `membershipSeedSub`** (переопределяются env **`MEMBERSHIP_SEED_ISSUER`**, **`MEMBERSHIP_SEED_SUB`**). Выполняется changeset **`014_membership_dev_seed.xml`** (tenant по умолчанию из `011`).
3. Вызовы **`GET /tenants/{id}`** в healenium-ai: при необходимости передаётся заголовок **`X-Healenium-Service-Key`** из **`HEALENIUM_AI_SERVICE_KEY`** (`healenium.ai.service-key` в `application-pro.yml`).
4. **`GET /healenium/me`**: **400 Bad Request**, если в JWT отсутствуют непустые **`iss`** и **`sub`**.

---

## Критерий готовности (коротко)

- UI: логин → **`/healenium/me`** → выбор tenant → запросы с **Bearer + `X-Tenant-Id`** → **200** на API backend.
- WebDriver: запросы с **`X-Tenant-Id`** без Bearer → **200**, tenant **ACTIVE** в AI.
- Пользователь без строки в **`membership`**: с Bearer на API → **403** (ожидаемо).
