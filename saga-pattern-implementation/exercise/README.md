# Saga Pattern Implementation

The Saga pattern manages data consistency in
distributed systems where a single ACID
transaction cannot span multiple services or
activities. Each step in a saga has a
corresponding **compensation** — an action that
undoes its effect if a later step fails.

In this exercise you will add the Temporal Saga
pattern to a greeting workflow so that when any
step fails, all previously completed steps are
compensated in strict reverse order.

## Objective

- Understand the difference between database
  rollback and saga compensation
- Create a `Saga` object with sequential
  compensation
- Register compensations after each successful
  activity
- Trigger `saga.compensate()` on failure to
  undo completed steps in reverse order
- Observe compensation behaviour for failures
  at different stages

## Prerequisites

- Java 21
- [Temporal CLI](https://docs.temporal.io/cli)
- Familiarity with Temporal workflows and
  activities (interfaces, implementations,
  task queues)
- Familiarity with Spring Boot basics

Start a local Temporal server:

```bash
temporal server start-dev
```

## Key Concepts

### The Saga pattern

The Saga pattern coordinates distributed
transactions through compensating actions rather
than ACID rollbacks. Each step in a saga has a
corresponding compensation — a real activity
call that reverses the step's effect.

Temporal's `Saga` class implements this in a
workflow:

1. Register a compensation immediately after
   each successful activity
2. On failure, call `saga.compensate()` to run
   all registered compensations in reverse order

Because compensations are Temporal activities,
they are durable: if the worker crashes
mid-compensation, Temporal will retry from where
it left off.

### Compensation vs rollback

A database rollback is atomic — either
everything succeeds or nothing does. A saga
compensation is a **business-level undo**: a
real activity call that reverses the effect.
Compensations can themselves fail and must be
designed to be retryable (idempotent).

### Registration order matters

`saga.addCompensation(...)` registers
compensations in forward order.
`saga.compensate()` runs them in reverse. Only
activities that have **already succeeded**
should have compensations registered.

## Steps

### Step 1 — Explore the project

Open the `exercise/` directory and review the
key classes:

```text
exercise/src/main/java/io/temporal/app/
├── domain/
│   ├── integrations/
│   │   ├── GreetingActivity.java       (interface — 6 methods)
│   │   └── GreetingActivityImpl.java   (implementation)
│   ├── messages/
│   │   └── Name.java                   (firstName + lastName)
│   └── workflows/greeting/
│       ├── GreetingWorkflow.java
│       └── GreetingWorkflowImpl.java   (TODO here)
└── api/controllers/
    └── GreetingRESTController.java
```

Note a few things:

- **`GreetingActivity`** already has
  `compensateN` methods — these are the
  compensation hooks you will wire up
- **`GreetingWorkflowImpl`** runs three
  activities in series but has no error
  handling — if `greet2` fails, `greet1`'s
  side-effects are never undone
- The `compensateN` implementations simply log
  the undo — in a real system these would
  reverse database writes, release
  reservations, etc.

Start the application and trigger a failure to
see the problem first:

```bash
./mvnw spring-boot:run
```

```bash
curl -s -X POST http://localhost:3030/greet \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Fail-2","lastName":"Forbes"}'
```

By default Temporal retries failing activities
indefinitely. The workflow will keep retrying
`greet2` forever. You can observe this in the
Temporal UI (`http://localhost:8233`).

### Step 2 — Disable retries on the activity options

Before adding the Saga, configure the
`ActivityOptions` to stop retrying on failure so
the `catch` block can fire immediately. Without
this, Temporal retries the failing activity
until the workflow timeout — the saga
compensation never runs.

In `GreetingWorkflowImpl`, add `setRetryOptions`
to the existing `ActivityOptions` builder:

```java
import io.temporal.common.RetryOptions;

private final ActivityOptions activityOptions =
        ActivityOptions.newBuilder()
                .setStartToCloseTimeout(
                        Duration.ofSeconds(10))
                .setRetryOptions(
                        RetryOptions.newBuilder()
                                .setMaximumAttempts(1)
                                .build())
                .build();
```

`setMaximumAttempts(1)` means one attempt, no
retries. When the activity fails, the exception
propagates immediately as an `ActivityFailure`,
which the `catch` block can intercept.

> **Note:** In production you would tune
> `maximumAttempts` based on the idempotency and
> retry-safety of each activity, not set it to 1
> globally. This setting is kept low here so the
> failure-and-compensate path is clearly visible
> during the exercise.

### Step 3 — Create a Saga object

At the top of `sayHello`, create a `Saga` with
sequential compensation so compensations run in
strict reverse order:

```java
import io.temporal.workflow.Saga;

Saga saga = new Saga(
        new Saga.Options.Builder()
                .setParallelCompensation(false)
                .build());
```

`setParallelCompensation(false)` means
compensations run one at a time in reverse
registration order — `compensate2` before
`compensate1`. Set it to `true` only when
compensations are independent and safe to run
concurrently.

### Step 4 — Register compensations after each successful step

Wrap the three activity calls in a `try` block.
After each successful call, register its
compensation:

```java
try {
    String result1 =
            greetingActivity.greet1(name);
    saga.addCompensation(
            greetingActivity::compensate1, name);

    Name name2 = new Name();
    name2.setFirstName(name.getFirstName());
    name2.setLastName(name.getLastName() + "-1");
    String result2 =
            greetingActivity.greet2(name2);
    saga.addCompensation(
            greetingActivity::compensate2, name2);

    Name name3 = new Name();
    name3.setFirstName(name.getFirstName());
    name3.setLastName(
            name2.getLastName() + "-2");
    return greetingActivity.greet3(name3);

} catch (ActivityFailure e) {
    // Step 5 goes here
    throw e;
}
```

The ordering rule: register a compensation
**only after the activity succeeds**. If
`greet1` fails, no compensation is registered
yet, so none runs. If `greet2` fails, only
`greet1`'s compensation is registered, so only
`compensate1` runs.

### Step 5 — Compensate on failure

Inside the `catch (ActivityFailure e)` block,
call `saga.compensate()`:

```java
} catch (ActivityFailure e) {
    saga.compensate();
    throw e;
}
```

`saga.compensate()` calls each registered
compensation in reverse order. Re-throwing the
exception ensures the workflow fails visibly in
the Temporal UI.

The full method should now look like this:

```java
import io.temporal.failure.ActivityFailure;

@Override
public String sayHello(Name name) {
    Saga saga = new Saga(
            new Saga.Options.Builder()
                    .setParallelCompensation(false)
                    .build());
    try {
        String result1 =
                greetingActivity.greet1(name);
        saga.addCompensation(
                greetingActivity::compensate1,
                name);

        Name name2 = new Name();
        name2.setFirstName(
                name.getFirstName());
        name2.setLastName(
                name.getLastName() + "-1");
        String result2 =
                greetingActivity.greet2(name2);
        saga.addCompensation(
                greetingActivity::compensate2,
                name2);

        Name name3 = new Name();
        name3.setFirstName(
                name.getFirstName());
        name3.setLastName(
                name2.getLastName() + "-2");
        return greetingActivity.greet3(name3);

    } catch (ActivityFailure e) {
        saga.compensate();
        throw e;
    }
}
```

### Step 6 — Run and observe compensation

Start the application:

```bash
./mvnw spring-boot:run
```

#### Happy path

```bash
curl -s -X POST http://localhost:3030/greet \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Donald","lastName":"Forbes"}'
```

Expected response: `Hello Donald Forbes-1-2-3`

The logs should show `greet1`, `greet2`,
`greet3` completing with no compensation.

#### Fail at step 2

```bash
curl -s -X POST http://localhost:3030/greet \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Fail-2","lastName":"Forbes"}'
```

Watch the application logs. You should see
`greet1` succeed, `greet2` fail, and then
`compensate1` run to undo the first step.
`greet3` and `compensate2` are never called
because `greet2` never completed.

#### Fail at step 3

```bash
curl -s -X POST http://localhost:3030/greet \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Fail-3","lastName":"Forbes"}'
```

Watch the logs. Both `greet1` and `greet2`
succeed, `greet3` fails, then `compensate2`
runs first followed by `compensate1` — strict
reverse order.

Compare the compensation order in each case:

| Failure point | Steps completed | Compensations (reverse order) |
|---------------|-----------------|-------------------------------|
| `greet1`      | none            | none                          |
| `greet2`      | greet1          | compensate1                   |
| `greet3`      | greet1, greet2  | compensate2, compensate1      |

### Step 7 — Run the tests

```bash
./mvnw test
```

The test suite covers all four cases: happy
path, fail at step 1, fail at step 2, and fail
at step 3. All tests should pass.

## Key Takeaways

1. The Saga pattern coordinates distributed
   transactions through compensating actions
   rather than ACID rollbacks.
2. Register a compensation **after** each
   successful activity — never before.
3. `setParallelCompensation(false)` runs
   compensations one at a time in strict
   reverse order.
4. Set `setMaximumAttempts(1)` on activity
   options to propagate failures immediately
   when building compensation logic.
5. Compensation activities are durable —
   Temporal retries them if the worker fails
   mid-compensation.
6. The `catch (ActivityFailure e)` block is the
   only place `saga.compensate()` should be
   called; always re-throw so the workflow fails
   visibly.

## Resources

- [Saga Pattern — Temporal Docs](https://docs.temporal.io/develop/java/failure-detection)
- [Saga class — Java SDK Javadoc](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/workflow/Saga.html)
- [Distributed Sagas: A Protocol for Long-Running Transactions (paper)](https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf)

## Solution

A working solution is available in the
`solution/` directory.
