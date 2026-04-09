# Activity Retry Metric with Activity Inbound Interceptor

This package demonstrates how to use an `ActivityInboundCallsInterceptor` to emit a custom metric when an activity is being retried, 
without modifying the activity implementation.

## Overview

The activity (`greet`) intentionally fails for the first 5 attempts to simulate transient failures. 
The interceptor observes the attempt number on every execution and emits a counter metric every 5th attempt.

---

## Interceptor Chain

```
WorkerInterceptor (RetryLoggingWorkerInterceptor)
    └─ interceptActivity()
        └─ RetryLoggingActivityInterceptor   [ActivityInboundCallsInterceptor]
```

`RetryLoggingWorkerInterceptor` is a Spring `@Component` active under the `interceptor-metric` profile. It leaves workflow interception untouched (`interceptWorkflow` returns `next` as-is) and wraps every activity execution with `RetryLoggingActivityInterceptor`.

---

## RetryLoggingActivityInterceptor

### `init(ActivityExecutionContext context)`

Called once before the activity executes. Saves the `ActivityExecutionContext` so it is available in `execute()`. Always delegates to `super.init(context)` to keep the chain intact.

### `execute(ActivityInput input)`

Called on every attempt, including retries.

1. Reads the current attempt number from `context.getInfo().getAttempt()`.
2. Emits a metric **only on every 5th attempt** (`attempt % 5 == 0`) to avoid flooding the metrics backend on every single retry.
3. Gets a `Scope` from `context.getMetricsScope()` and tags it with the `workflow_run_id`.
4. Increments the `activity_retry` counter by the current attempt number.
5. Delegates to `super.execute(input)` to actually run the activity.

```java
if (attempt % 5 == 0) {
    Scope scope = context.getMetricsScope()
            .tagged(Map.of("workflow_run_id", info.getRunId()));
    scope.counter("activity_retry").inc(attempt);
}
return super.execute(input);
```

> Tagging metrics with `workflow_run_id` creates one unique metric series per workflow run.

---

## Activity Implementation

`HelloActivityInterceptorImpl.greet()` simulates a transient failure by throwing a `RuntimeException` for attempts 1–5. On attempt 6 and beyond it succeeds:

```java
if (Activity.getExecutionContext().getInfo().getAttempt() < 6) {
    throw new RuntimeException("Simulating a transient failure");
}
return "Hello, " + name + "!";
```

The workflow configures no backoff (`backoffCoefficient = 1.0`) so retries happen immediately, making it easy to observe the metric during development.

---

## End-to-End Flow

```
StarterRunner
  └─ WorkflowClient.start(sayHello, "Temporal")
       │
       └─ HelloInterceptorWorkflowImpl.sayHello()
            │
            └─ helloActivity.greet("Temporal")
                 │
                 └─ RetryLoggingActivityInterceptor.execute()   ← attempt 1
                      ├─ attempt % 5 != 0, no metric emitted
                      └─ super.execute()  →  greet() throws RuntimeException
                 │
                 └─ RetryLoggingActivityInterceptor.execute()   ← attempt 2, 3, 4
                      └─ (same, no metric)
                 │
                 └─ RetryLoggingActivityInterceptor.execute()   ← attempt 5
                      ├─ attempt % 5 == 0  →  activity_retry counter += 5  (tagged: workflow_run_id)
                      └─ super.execute()  →  greet() throws RuntimeException
                 │
                 └─ RetryLoggingActivityInterceptor.execute()   ← attempt 6
                      ├─ attempt % 5 != 0, no metric emitted
                      └─ super.execute()  →  greet() returns "Hello, Temporal!"
```
