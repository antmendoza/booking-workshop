# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Prerequisites

- Java 21
- Temporal server running locally: `temporal server start-dev` (serves on `127.0.0.1:7233`, UI at `http://localhost:8233`)

## Common Commands

All commands run from `exercise1/`:

```bash
# Run tests
./mvnw test

# Run a specific test
./mvnw test -Dtest=HelloWorldWorkflowTest

# Build without tests
./mvnw package -DskipTests

# Run with a specific exercise profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=hello
./mvnw spring-boot:run -Dspring-boot.run.profiles=interceptor-metric
./mvnw spring-boot:run -Dspring-boot.run.profiles=interceptor-localactivity-auth

# Check retry metrics (requires running app)
curl -s http://localhost:3030/actuator/prometheus | grep '^activity_retry'
```

## Architecture

This is a Spring Boot + Temporal SDK workshop with multiple exercises, each activated via Spring profiles. The single application module (`exercise1/`) contains all exercises co-located under separate packages.

### Profile-per-exercise pattern

Each exercise lives in its own package under `src/main/java/io/temporal/workflow/` and is activated by a Spring profile:

| Profile | Package | Purpose |
|---|---|---|
| `hello` | `workflow.hello` | Basic workflow/activity pattern |
| `interceptor-metric` | `workflow.interceptor.metric` | Retry observability via `WorkerInterceptor` |
| `interceptor-localactivity-auth` | `workflow.interceptor.localactivity.auth` | Auth token propagation + local activity token refresh |

Each profile has a corresponding `application-{profile}.yml` that configures namespace and task queue auto-discovery for that package only.

### Temporal Spring Boot integration

- Workflows implement an `@WorkflowInterface` and are annotated `@WorkflowImpl(taskQueues = "...")` — Spring Boot auto-registers them as workers.
- Activities implement an `@ActivityInterface` and are annotated `@ActivityImpl(taskQueues = "...")` — auto-registered as Spring `@Component` beans.
- `StarterRunner` (per exercise) implements `ApplicationRunner` to kick off a workflow execution on startup.

### Exercise 2 — Retry metrics interceptor

`RetryLoggingWorkerInterceptor` wraps activity execution via `ActivityInboundCallsInterceptor`. On every 5th attempt it emits a Prometheus counter (`activity_retry_count_total`) tagged with `workflow_run_id`. The activity itself intentionally throws for the first 5 attempts to simulate transient failures.

### Exercise 3 — Auth context propagation + interceptor chain

Three-layer design:
1. **`MDCContextPropagator`** — serializes MDC keys prefixed `x-auth-` into Temporal workflow headers so auth tokens flow from starter → workflow worker → activity worker automatically.
2. **`ActivityAuthOutboundInterceptor`** — intercepts outgoing activity schedules. If the activity fails with `ApplicationFailure` type `TokenExpired`, it calls a local activity to regenerate the token, updates MDC, then retries the original activity.
3. **`TemporalOptionsConfig`** — Spring `@Configuration` that registers the context propagator on `WorkflowClientOptions`.

### Testing approach

Tests use `TestWorkflowEnvironment` (in-process, no live server needed). Activities are mocked with Mockito; workflows run against the mock. See `HelloWorldWorkflowTest.java` for the pattern.
