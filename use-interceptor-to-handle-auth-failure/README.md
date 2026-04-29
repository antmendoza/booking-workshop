# Using an Interceptor to Handle Auth Failures

Temporal's **WorkflowOutboundCallsInterceptor**
lets you intercept every Activity dispatch inside
a Workflow execution. Combined with a
**ContextPropagator** that carries the token
through the Temporal execution boundary, the
interceptor can catch a `TokenExpired` failure,
call a token-refresh Local Activity, update the
propagated context, and replay the original
Activity — all invisible to the Workflow
implementation.

In this exercise you will implement the
catch-and-refresh logic in
`ActivityAuthOutboundInterceptor`.

## Objective

- Understand how `ContextPropagator` carries
  MDC keys across Workflow and Activity boundaries
- Implement the `catch (ActivityFailure e)` block
  in `ActivityAuthOutboundInterceptor.executeActivity()`
  to detect a `TokenExpired` error, call
  `regenerateAuthToken()` as a Local Activity,
  update the MDC with the new token, and retry the
  original Activity
- Verify the Workflow still passes the unit tests
- Run the application with the
  `interceptor-localactivity-auth` profile and
  confirm the Workflow completes despite starting
  with an expired token

## Prerequisites

- Java 21
- [Temporal CLI](https://docs.temporal.io/cli)
- Familiarity with Temporal Workflows and
  Activities
- Familiarity with Spring Boot basics

Start a local Temporal dev server:

```bash
task temporal:start
```

Or, without [Task](https://taskfile.dev):

```bash
temporal server start-dev
```

The server listens on `127.0.0.1:7233` with the
Web UI at `http://localhost:8233`.

## Key Concepts

### ContextPropagator

A `ContextPropagator` serialises caller-side
context (e.g. MDC entries) into Temporal headers
so that it is restored in the Activity thread on
the Worker side. Without it, MDC values set by the
client are lost when the Activity executes on a
Worker thread.

`MDCContextPropagator` in this exercise:

- **`getCurrentContext()`** — snapshots every MDC
  key that starts with `x-auth-` at dispatch time
- **`serializeContext()` / `deserializeContext()`**
  — encode/decode the snapshot as Temporal Payload
  values
- **`setCurrentContext()`** — restores the keys
  into MDC on the receiving thread

`TemporalOptionsConfig` wires the propagator into
the SDK via a `TemporalOptionsCustomizer` bean:

```java
optionsBuilder.setContextPropagators(
    Collections.singletonList(
        new MDCContextPropagator()));
```

### WorkflowOutboundCallsInterceptor

Intercepting at the outbound level means wrapping
every `executeActivity` call that the Workflow
makes, rather than wrapping each individual
Activity implementation. This is the right layer
for cross-cutting retry logic: one place covers
all Activities dispatched by any Workflow running
on the Worker.

The interceptor chain for this exercise is:

```
ActivityAuthWorkerInterceptor       (WorkerInterceptor)
  └─ interceptWorkflow()
       └─ ActivityAuthInboundInterceptor   (WorkflowInboundCallsInterceptor)
            └─ init()
                 └─ ActivityAuthOutboundInterceptor  (WorkflowOutboundCallsInterceptor)
                      └─ executeActivity()  ← TODO
```

`ActivityAuthWorkerInterceptor` is a Spring
`@Component` and `@Profile("interceptor-localactivity-auth")`
bean — the Temporal Spring Boot starter picks it
up and registers it on every Worker automatically.

### Local Activity for Token Refresh

`regenerateAuthToken()` is called as a **Local
Activity**. Local Activities run in the same
process as the Workflow, execute quickly, and do
not create a separate Activity Task on the Task
Queue. This makes them ideal for lightweight
side-effects such as fetching a fresh token from
a local cache or secret store.

The stub is declared inside
`ActivityAuthOutboundInterceptor` with a 3-second
`scheduleToCloseTimeout` and `maxAttempts=1`:

```java
private final HelloActivityInterceptor myActivities =
    Workflow.newLocalActivityStub(
        HelloActivityInterceptor.class,
        LocalActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofSeconds(3))
            .setRetryOptions(
                RetryOptions.newBuilder()
                    .setMaximumAttempts(1).build())
            .build());
```

## Steps

### Step 1 — Explore the project

Open `exercise/`. The package
`io.temporal.workshops.springboot.interceptor
.localactivity.auth` contains:

- **`HelloInterceptorWorkflow`** — a Workflow
  interface with a single `sayHello(String name)`
  method
- **`HelloInterceptorWorkflowImpl`** — registers
  on the `HelloInterceptorLocalactivityAuth` Task
  Queue; calls `one`, `two`, and `three` Activities
  in sequence
- **`HelloActivityInterceptor`** — Activity
  interface with `one`, `two`, `three`, and
  `regenerateAuthToken` methods
- **`HelloActivityInterceptorImpl`** — each of
  `one`, `two`, `three` reads `x-auth-jwt-token`
  from MDC and throws a non-retryable
  `ApplicationFailure` with type `"TokenExpired"`
  when the token equals `"expired-token"`;
  `regenerateAuthToken` returns `"new-valid-token"`
- **`MDCContextPropagator`** — propagates
  `x-auth-*` MDC entries across execution
  boundaries
- **`TemporalOptionsConfig`** — registers
  `MDCContextPropagator` with the
  `WorkflowClient` via `TemporalOptionsCustomizer`
- **`ActivityAuthWorkerInterceptor`** — Spring
  `@Component` that wraps Workflow execution with
  `ActivityAuthInboundInterceptor`
- **`ActivityAuthInboundInterceptor`** —
  `WorkflowInboundCallsInterceptorBase` that
  replaces the outbound calls with
  `ActivityAuthOutboundInterceptor`
- **`ActivityAuthOutboundInterceptor`** —
  `WorkflowOutboundCallsInterceptorBase`; the
  `executeActivity()` method contains the TODO
  you will implement
- **`StarterRunner`** — an `ApplicationRunner`
  that puts `"expired-token"` in MDC before
  starting a `HelloInterceptorWorkflow` execution,
  simulating an expired auth token

### Step 2 — Implement the auth-refresh logic

Open `ActivityAuthOutboundInterceptor.java`.
The `executeActivity()` method already calls
`super.executeActivity(input)` and blocks on the
result. The empty `catch (ActivityFailure e)`
block is where you will add the refresh logic.

Implement the catch block:

1. Cast `e.getCause()` to `ApplicationFailure`
   and check its type equals `"TokenExpired"`
2. Call `myActivities.regenerateAuthToken()` to
   get a fresh token
3. Store the new token with
   `MDC.put("x-auth-jwt-token", newToken)`
4. Retry the original activity with
   `rActivityOutput = super.executeActivity(input)`

The completed block should look like this:

```java
} catch (ActivityFailure e) {

        if (((ApplicationFailure) e.getCause()).getType().equals("TokenExpired")) {
            log.error("Error executing activity: {}", e.getMessage());

            // generate a new token
            String newToken = myActivities.regenerateAuthToken();
                            MDC.put("x-auth-jwt-token", newToken);
            
            // retry the original activity
            rActivityOutput = super.executeActivity(input);

        }
}

```

### Step 3 — Verify with the unit tests

The `HelloInterceptorWorkflowTest` class uses
`TestWorkflowEnvironment` with a mock Activity
that always returns successfully, confirming the
Workflow mechanics compile and the Activity
sequence executes correctly.

Run the tests:

```bash
cd exercise
./mvnw test
```

### Step 4 — Run the application

Start the application with the
`interceptor-localactivity-auth` profile:

```bash
cd exercise
./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=interceptor-localactivity-auth
```

`StarterRunner` sets `x-auth-jwt-token` to
`"expired-token"` in MDC and starts a
`HelloInterceptorWorkflow` execution. The first
Activity call (`one`) will fail immediately with
`TokenExpired`. Watch the logs for the interceptor
catching the error, calling `regenerateAuthToken`,
and retrying.

### Step 5 — Observe the Workflow

Open the Temporal Web UI at
`http://localhost:8233` and locate the running
Workflow Execution. In the Event History you will
see:

1. `ActivityTaskScheduled` → `ActivityTaskFailed`
   (TokenExpired) for the first attempt of `one`
2. A `LocalActivityMarker` recording the
   `regenerateAuthToken` call
3. `ActivityTaskScheduled` → `ActivityTaskCompleted`
   for the retried `one`, then `two`, then `three`

The Workflow completes successfully despite
starting with an expired token.

## Key Takeaways

1. **ContextPropagators** carry request-scoped
   state (such as auth tokens) from the caller
   through Temporal execution boundaries, making
   it available inside Activity threads without
   any explicit parameter passing.
2. **WorkflowOutboundCallsInterceptor** is the
   right interception point for cross-cutting
   retry policies that span multiple Activities —
   the logic lives in one place and applies to
   every Activity the Workflow dispatches.
3. **Local Activities** are the correct tool for
   lightweight side-effects such as refreshing a
   token: they run in-process, are fast, and do
   not incur Task Queue scheduling overhead.
4. Marking the `ApplicationFailure` as
   **non-retryable** (`newNonRetryableFailure`)
   in the Activity prevents the Temporal SDK
   from retrying at the Activity level, giving
   the interceptor full control over the retry
   strategy.
5. The **Temporal Spring Boot starter** picks up
   any `WorkerInterceptor` `@Component` bean
   automatically — no manual Worker configuration
   is needed.

## Resources

- [WorkerInterceptor — Java SDK Javadoc](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/interceptors/WorkerInterceptor.html)
- [WorkflowOutboundCallsInterceptor — Java SDK Javadoc](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/interceptors/WorkflowOutboundCallsInterceptor.html)
- [ContextPropagator — Java SDK Javadoc](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/context/ContextPropagator.html)
- [Context Propagation — Temporal Docs](https://docs.temporal.io/develop/java/observability#context-propagation)
- [Local Activities — Temporal Docs](https://docs.temporal.io/activities#local-activity)
- [Spring Boot integration — Java SDK](https://docs.temporal.io/develop/java/spring-boot-integration)

## Solution

A working solution is available in the
`solution/` directory.