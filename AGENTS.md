# AGENTS.md

## Project Overview

Micronaut 5.1.0 microservice (Java 25, Maven) for managing donation actions in the OpenDonationAssistant platform. Consumes RabbitMQ events, exposes REST endpoints, persists to PostgreSQL.

## Build & Test

```bash
mvn clean package          # full build (compiles, runs tests, packages jar)
mvn test                   # run tests
mvn -DskipTests package    # build without tests
mvn -Dpackaging=native-image clean package  # GraalVM native image (CI uses this)
```

- No `mvnw` wrapper script at root — use system `mvn`.
- Requires Java 25 and the `oda-libraries` GitHub Packages Maven repo (configured via `~/.m2/settings.xml` with `MAVEN_USER`/`MAVEN_TOKEN`).
- **NullAway + ErrorProne are enforced as compile-time errors** (`-Xep:NullAway:ERROR`). All code must use `@Nonnull`/`@Nullable` (JSpecify) annotations. NullAway only checks `io.github.opendonationassistant` packages.
- Micronaut AOT is disabled for jar packaging (`micronaut.aot.enabled=false`); AOT config lives in `aot-jar.properties`.

## Architecture

```
src/main/java/io/github/opendonationassistant/
  api/       # REST endpoint interfaces (@Controller interfaces with Swagger annotations + DTO records)
  commands/  # Command implementations (AddAction, EnableActions, DisableActions, DeleteActions, LinkActions)
  listeners/ # RabbitMQ event consumers (EventsListener → handlers)
  repository/ # Domain entities + data repositories (Action, ActionRequest, *Data, *Repository)
  view/      # Read-only query controllers (ActionController)
```

- **API pattern**: Each endpoint is declared as a `@Controller` interface in `api/` with a `default` method that delegates to an abstract `execute()` method. The concrete implementation lives in `commands/` or `view/` and implements the interface.
- **DTOs are records** annotated with `@Serdeable` and defined as nested records inside the API interfaces.
- **Domain model**: `Action` wraps `ActionData` (a `@MappedEntity` record) and handles save/delete/enable/disable, each of which also sends a `ConfigCommand` via RabbitMQ to sync downstream services.
- **Event flow**: `EventsListener` consumes from the `action.events` RabbitMQ queue. `Application.rabbitConfiguration()` wires exchanges (`history`, `payments`) to this queue via `oda-rabbit-conf`.
- **Shared code** comes from `oda-commons` (e.g., `BaseController`, `ODALogger`, `Amount`) and `oda-rabbit-conf` (event types, senders, `AMQPConfiguration`), both versioned by the `oda.version` property in `pom.xml`.

## Database

- PostgreSQL, schema `actions` (set in `application.yml` and `application-standalone.yml`).
- Flyway migrations in `src/main/resources/db/migration/` (V1, V2, V3). **Never edit applied migrations** — add a new V4+.
- Tables: `action` (id, recipient_id, category, provider, name, amount, game, enabled, payload jsonb) and `action_request` (id, source, origin_id, actions jsonb).
- `@MappedEntity` records map to tables; `@MappedProperty(type = DataType.JSON)` for JSON columns.

## Configuration & Environments

- Default environment is `standalone` (set in `Application.DefaultEnvironmentConfigurer`). `application-standalone.yml` provides local PostgreSQL defaults.
- `application-allinone.yml` is an alternate profile (DEBUG logging, postgres db-type).
- Required env vars: `JDBC_URL`, `JDBC_USER`, `JDBC_PASSWORD`, `JWKS_URI` (Keycloak JWT signing key URL).
- Security: JWT via Keycloak JWKS. `@Secured(SecurityRule.IS_AUTHENTICATED)` for write commands, `IS_ANONYMOUS` for reads.
- **Deprecated endpoints**: Each command API exposes both kebab-case (`/actions/commands/add-actions`) and camelCase (`/actions/commands/addActions`) paths. The camelCase ones are `@Hidden` from Swagger but still active — keep both in sync.

## Testing

- Pure unit tests with Mockito + Instancio (`@Given` for random test data). No integration tests, no test resources.
- `oda-test-utils` provides Instancio generators (e.g., `AuthenticationGenerator`, `AmountGenerator`).
- `micronaut.test.resources.enabled=true` in pom but tests don't use test containers — they're plain JUnit 5 + Mockito.
- Run a single test: `mvn test -Dtest=AddActionTest`
- `TEST-REPORT.md` documents known coverage gaps (12 classes with zero tests).

## CI/CD

- `.github/workflows/release.yml` triggers on push to `master` (or manual dispatch).
- Delegates to `OpenDonationAssistant/oda-libraries/.github/workflows/release_service.yml@master` which: runs Sonar analysis, builds native image, tags the commit, pushes Docker image to GHCR, publishes OpenAPI spec to the docs repo, and generates an npm client package.
- Version is `${{ github.RUN_NUMBER }}` — no manual version bumps needed.

## Logging

- JSON-formatted logs via `logback.xml` (structured fields: timestamp, level, logger, context, errors, message).
- Use `ODALogger` (from `oda-commons`) instead of raw SLF4J loggers — it provides structured logging helpers.
- `application-allinone.yml` sets `io.github.opendonationassistant` to DEBUG.

## Gotchas

- **NullAway is strict**: any unannotated nullable parameter/field will fail compilation. Always annotate with `@Nonnull`/`@Nullable`.
- **`Action.enable()`/`disable()`** send different RabbitMQ commands: enable → `UpsertAction`, disable → `DeleteAction`.
- **`PaymentEventHandler` throws `RuntimeException("Action not found")`** when a payment references a non-existent action — this is intentional.
- **`LinkActions`** always returns `new Amount(0, 0, "RUB")` regardless of the request — the actual amount is computed downstream.
- **`ActionController.getActions`** returns 401 when `recipientId` is empty (not 400).
- The `target/` directory is gitignored; generated Micronaut sources land in `target/generated-sources/annotations/`.
