# Auth Token Propagation with Context Propagator and Interceptors

This package demonstrates how to propagate an auth token from a workflow starter through to activity execution, 
and how to transparently refresh an expired token using Temporal interceptors.

## Overview

The flow has two concerns:

1. **Context Propagation** — carry the auth token from the starter's MDC, through Temporal's headers, into the activity worker's MDC.
2. **Interception** — detect a `TokenExpired` failure mid-workflow, refresh the token via a local activity, and retry the original activity transparently.

---

## Context Propagation

### How it works

Temporal supports `ContextPropagator` to pass custom data through workflow and activity headers automatically. `MDCContextPropagator` implements this interface.

```
Starter (MDC)  →  Temporal headers  →  Activity worker (MDC)
```

The three methods that matter:

| Method | Direction | What it does |
|---|---|---|
| `getCurrentContext()` | Outbound | Reads MDC keys prefixed `x-auth-` and returns them as a map |
| `serializeContext()` | Outbound | Encodes that map into Temporal `Payload` headers |
| `deserializeContext()` | Inbound | Decodes the headers back into a string map |
| `setCurrentContext()` | Inbound | Writes the decoded map back into MDC on the worker thread |

Only keys starting with `x-auth-` are propagated — everything else in MDC is ignored.

### Registration

`TemporalOptionsConfig` registers `MDCContextPropagator` on the `WorkflowClientOptions` so it applies to all workflow and activity calls made by this client:

```java
@Bean
public TemporalOptionsCustomizer<WorkflowClientOptions.Builder> customClientOptions() {
    return optionsBuilder -> {
        optionsBuilder.setContextPropagators(
            Collections.singletonList(new MDCContextPropagator())
        );
        return optionsBuilder;
    };
}
```

### Starter

`StarterRunner` seeds the token into MDC before starting the workflow:

```java
MDC.put("jwt-token", "invalid-token");
WorkflowClient.start(workflow::sayHello, "Temporal");
```

---

## Interceptors

Three interceptor classes form a chain that is wired together per workflow execution.

### Wiring

```
WorkerInterceptor (ActivityAuthWorkerInterceptor)
    └─ interceptWorkflow()
        └─ ActivityAuthInboundInterceptor   [WorkflowInboundCallsInterceptor]
            └─ init()
                └─ ActivityAuthOutboundInterceptor  [WorkflowOutboundCallsInterceptor]
```

`ActivityAuthWorkerInterceptor` is a Spring `@Component` registered with the Temporal worker. For each workflow execution it creates an `ActivityAuthInboundInterceptor`. That inbound interceptor's `init()` method wraps the outbound calls interceptor with `ActivityAuthOutboundInterceptor`.

### ActivityAuthOutboundInterceptor

This is where the token-refresh logic lives. It overrides two methods:

#### `executeActivity`

Intercepts every regular activity scheduled by the workflow.

1. Calls `super.executeActivity(input)` to schedule the activity.
2. Blocks on `.getResult().get()` to surface any failure synchronously.
3. If the failure cause is `TokenExpired`:
   - Calls `myActivities.regenerateAuthToken()` — a local activity stub defined as a field — to obtain a new token.
   - Updates MDC: `MDC.put("x-auth-jwt-token", newToken)`.
   - Retries the original activity via `super.executeActivity(input)`. The `MDCContextPropagator` will serialize the updated MDC into headers for the retry.

> **Why `.getResult().get()` is needed:** `super.executeActivity()` is non-blocking — it schedules the activity and returns a future. The exception is only thrown when the result is consumed. Without `.get()`, the catch block never fires.

---

## Activity Implementation

`HelloActivityInterceptorImpl` simulates token validation in `anyHttpRequest()`:

```java
String token = MDC.get("x-auth-jwt-token");
if (token.equals("expired-token")) {
    throw ApplicationFailure.newNonRetryableFailure("Token is expired", "TokenExpired");
}
```

`regenerateAuthToken()` returns a new valid token string, simulating a token refresh call to an auth service.

---

## End-to-End Flow

```
StarterRunner
  MDC.put("x-auth-jwt-token", "expired-token")
  │
  └─ WorkflowClient.start(sayHello)
       │   (MDCContextPropagator serializes x-auth-* keys into Temporal headers)
       │
       └─ HelloInterceptorWorkflowImpl.sayHello()
            │
            └─ helloActivity.one()  ← regular activity stub
                 │
                 └─ ActivityAuthOutboundInterceptor.executeActivity()
                      ├─ super.executeActivity(input)
                      ├─ .getResult().get()  ← blocks, throws ActivityFailure(TokenExpired)
                      ├─ myActivities.regenerateAuthToken()  → returns "new-valid-token"
                      ├─ MDC.put("x-auth-jwt-token", "new-valid-token")
                      └─ super.executeActivity(input)  ← retry with updated token in headers
                           │
                           └─ HelloActivityInterceptorImpl.one()
                                └─ anyHttpRequest()  ← token is now valid, succeeds
```
