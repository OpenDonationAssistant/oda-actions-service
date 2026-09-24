# Test Coverage Analysis Report

> Generated: 2026-07-28
> Project: oda-actions-service
> Framework: Micronaut 5.0.2 / Java 25 / Maven
> Coverage Tool: JaCoCo (configured, no threshold set)

---

## Current State

| Metric | Count |
|--------|-------|
| Source files | 23 |
| Existing test files | 2 |
| Classes with **zero** tests | **12** |
| Missing test scenarios (estimated) | **80+** |
| Test resource files | **0** |
| Coverage target configured | **No** |

---

## 🔴 CRITICAL GAPS — Entire Classes Without Tests

| Class | Package | Lines | Risk |
|-------|---------|-------|------|
| `EnableActions` | commands | 97 | **No test at all** |
| `DisableActions` | commands | 96 | **No test at all** |
| `DeleteActions` | commands | 57 | **No test at all** |
| `LinkActions` | commands | 52 | **No test at all** |
| `Action` (domain entity) | repository | 70 | **No test at all** |
| `ActionRepository` | repository | 90 | **No test at all** |
| `ActionRequestRepository` | repository | 33 | **No test at all** |
| `ActionRequest` | repository | 28 | **No test at all** |
| `HistoryEventHandler` | listeners/handlers | 65 | **No test at all** |
| `PaymentEventHandler` | listeners/handlers | 54 | **No test at all** |
| `EventsListener` | listeners | 28 | **No test at all** |
| `Application` | root | 46 | **No test at all** |

---

## 🟡 HIGH PRIORITY — Missing Scenarios in Existing Tests

### `AddActionTest` (1 test exists, 6+ missing scenarios)

| Missing Scenario | Why It Matters |
|---|---|
| **Unauthorized** — `getOwnerId(auth)` returns empty | Returns 401; auth failure is a security boundary |
| **Multiple actions** — command with 2+ `NewAction` entries | The `reduce`/`thenCombine` pattern is untested |
| **Empty actions list** — `command.actions()` is empty | Edge case; reduce on empty stream |
| **Repository failure** — `repository.create()` completes exceptionally | Error propagation through CompletableFuture chain |
| **Null optional fields** — null `category`, `game`, or `payload` | These flow through to `ActionData` creation |
| **Deprecated endpoint** — `/actions/commands/addActions` | Legacy path still active |

### `ActionControllerTest` (1 test exists, 8+ missing scenarios)

| Missing Scenario | Why It Matters |
|---|---|
| **Unauthorized** — empty `recipientId` | Returns 401; security boundary |
| **Filter by `game`** | Query parameter filtering logic |
| **Filter by `enabled`** | Boolean filter; different from string filters |
| **Filter by `category`** | Query parameter filtering logic |
| **Filter by `provider`** | Query parameter filtering logic |
| **Combined filters** — e.g., game + category + enabled | Multiple predicates via `builder.and()` |
| **Empty results** — no actions match filters | Returns empty list vs null |
| **Null fields in ActionData** — null `category`, `game`, `enabled` | `ActionDto.from()` uses `Optional.ofNullable().orElse("")` |

---

## 🟡 HIGH PRIORITY — Commands With Zero Tests

### `EnableActions` — 8+ missing scenarios

| Scenario | Details |
|---|---|
| **Happy path — enable by IDs** | `command.ids()` with valid IDs |
| **Unauthorized** | `getOwnerId(auth)` returns empty → 401 |
| **Enable by `game` filter** | No IDs, only game filter |
| **Enable by `category` filter** | No IDs, only category filter |
| **Enable by `provider` filter** | No IDs, only provider filter |
| **Combined filters** | game + category + provider together |
| **Null `ids`** | `command.ids()` is null → uses spec only |
| **Non-existent IDs** | IDs not found → `findByIdAndRecipientId` returns empty |
| **Mixed scenario** | Some IDs found, some not |
| **Repository failure** | `Action.enable()` future completes exceptionally |

### `DisableActions` — 8+ missing scenarios

*(Identical filter/spec pattern to EnableActions)*

| Scenario | Details |
|---|---|
| Happy path by IDs | Unauthorized | By game | By category | By provider |
| Combined filters | Null ids | Non-existent IDs | Mixed | Failure |

### `DeleteActions` — 7+ missing scenarios

| Scenario | Details |
|---|---|
| **Happy path — delete by IDs** | Valid IDs, actions found and deleted |
| **Unauthorized** | `getOwnerId(auth)` returns empty → 401 |
| **Null `ids`** | `command.ids()` is null → `idsOpt.orElse(List.of())` → empty stream |
| **Empty ids list** | No IDs to delete |
| **Non-existent IDs** | `findByIdAndRecipientId` returns empty → `orElseGet(completedFuture(null))` |
| **Mixed scenario** | Some found (deleted), some not (skipped) |
| **Repository failure** | `Action.delete()` future completes exceptionally |

### `LinkActions` — 4+ missing scenarios

| Scenario | Details |
|---|---|
| **Happy path** | Valid request with actions, saves ActionRequestData |
| **Empty actions list** | Request with no actions |
| **Null `originId`** | Null originId in request |
| **Repository failure** | `save()` throws |

---

## 🟡 HIGH PRIORITY — Domain Entity (`Action`)

| Scenario | Details |
|---|---|
| **`save()` happy path** | Calls `repository.save(data)` then `updateConfig()` → sends `UpsertAction` |
| **`delete()` happy path** | Calls `repository.delete(data)` then `deleteConfig()` → sends `DeleteAction` |
| **`enable()` happy path** | Updates data with `enabled=true`, calls `repository.update()`, sends `UpsertAction` |
| **`disable()` happy path** | Updates data with `enabled=false`, calls `repository.update()`, sends `DeleteAction` |
| **Null `category` in `updateConfig()`** | `Optional.ofNullable(data.category()).orElse("")` |
| **Null `game` in `updateConfig()`** | `Optional.ofNullable(data.game()).orElse("")` |

---

## 🟡 HIGH PRIORITY — Event Handlers

### `HistoryEventHandler` — 6+ missing scenarios

| Scenario | Details |
|---|---|
| **Happy path** | Valid `HistoryItemEvent` with originId → finds action requests → resolves action names → sends `ActionHistoryEvent` |
| **Null `originId`** | Early return — `if (originId == null) return;` |
| **No matching action requests** | `findByOriginId` returns empty list |
| **Action link references non-existent action** | `findByActionId` returns empty → filtered out by `flatMap` |
| **Empty actions after resolution** | `actions.size() > 0` check prevents sending empty event |
| **Facade send failure** | `facade.sendEvent().join()` throws |

### `PaymentEventHandler` — 4+ missing scenarios

| Scenario | Details |
|---|---|
| **Happy path** | Valid `PaymentEvent` → resolves actions → sends `ActionRequest` |
| **Action not found** | `findByIdAndRecipientId` returns empty → **throws `RuntimeException("Action not found")`** |
| **Empty actions in payment** | `payment.actions()` is empty |
| **Request sender failure** | `requestSender.send()` throws |

---

## 🔵 MEDIUM PRIORITY — Repository Layer

| Class | Missing Scenarios |
|---|---|
| **`ActionRepository`** | `findByIdAndRecipientId` (found/not found), `list()` returns actions, `findByActionId` (found/not found), `create()` sets `enabled=true` by default, `findAll(spec)` with various specs |
| **`ActionRequestRepository`** | `findByOriginId` (found/not found), `create()` returns ActionRequest |
| **`ActionData.withEnabled()`** | Creates new record with updated enabled field |
| **`ActionDto.from()`** | Null category → empty string, null game → empty string, all fields present |

---

## 🔵 MEDIUM PRIORITY — Integration / System Tests

| Scenario | Details |
|---|---|
| **Full CRUD lifecycle** | Create → List → Enable → Disable → Delete |
| **RabbitMQ configuration** | `Application.rabbitConfiguration()` creates correct exchanges/queues |
| **Database migrations** | V1, V2, V3 Flyway migrations apply correctly |
| **End-to-end: payment received** | PaymentEvent → ActionRequestSender → action execution |
| **End-to-end: history event** | HistoryItemEvent → ActionHistoryEvent → facade |

---

## Recommended Priority Order for Adding Tests

1. **`EnableActions`** + **`DisableActions`** — identical complex filter logic, high business value
2. **`DeleteActions`** — destructive operation, needs safety verification
3. **`Action`** domain entity — core business logic (save/delete/enable/disable)
4. **`PaymentEventHandler`** — has a `throw new RuntimeException` on not-found
5. **`HistoryEventHandler`** — complex multi-repository orchestration
6. **`LinkActions`** — creates payment-bound action requests
7. **`ActionRepository`** — repository integration with specs
8. **Expand `AddActionTest`** — unauthorized, multiple actions, error cases
9. **Expand `ActionControllerTest`** — all filter combinations, empty results
10. **`EventsListener`** + **`Application`** — infrastructure verification