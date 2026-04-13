# Testing Temporal Workflows with Spring Boot

The Temporal Spring Boot starter integrates
with the Spring Boot Test framework through
the `temporal-testing` module. When the test
server is enabled, the auto-configuration
provides an in-memory Temporal service.
Workflows and activities are auto-discovered
and registered exactly as in production — no
manual worker setup required. Additionally,
replay testing lets you verify that code
changes are backwards-compatible with
already-running workflow executions.

In this exercise you will write three
different kinds of tests for the Hello
workflow: a Spring Boot integration test with
the real activity, a Mockito-based test with
a mocked activity, and a replay test.

## Objective

Write tests for a Temporal workflow that:

- Use `@SpringBootTest` with the in-memory
  Temporal test server
- Mock activities using `@MockitoBean`
- Replay a workflow execution from a captured
  history

## Prerequisites

- Java 21
- Familiarity with Temporal workflows and
  activities (interfaces, implementations,
  task queues)
- Familiarity with JUnit 5 and Spring Boot
  Test basics
- **No running Temporal server needed** —
  tests use the in-memory test server

## Steps

### Step 1 — Explore the project

Open the `exercise/` directory. A working
Spring Boot application is already provided
with `HelloWorkflow` and `HelloActivity`
implemented in the `hello` package.

Look at the test configuration in
`src/test/resources/application-test.yaml` —
it enables the in-memory Temporal test server:

```yaml
spring:
  main:
    allow-bean-definition-overriding: true
  temporal:
    test-server:
      enabled: true
```

This profile-based configuration extends
`src/main/resources/application.yaml` without
overriding the `workersAutoDiscovery` setting.
Tests activate it with
`@ActiveProfiles("test")`.

Look at the `src/test/` directory — it
contains three test files with TODO
instructions:

- **`HelloWorkflowTest.java`** — tests the
  workflow with the real activity
  implementation
- **`HelloWorkflowMockitoTest.java`** — tests
  the workflow using `@MockitoBean` to mock
  the activity
- **`HelloWorkflowReplayTest.java`** —
  captures a workflow history and replays it
  against the current workflow implementation

### Step 2 — Write the workflow integration test

Open `HelloWorkflowTest.java`. This test uses
`@SpringBootTest` to load the full application
context with the in-memory Temporal test
server.

The class is already annotated with
`@SpringBootTest` and
`@ActiveProfiles("test")`.

1. Inject the `WorkflowClient` using
   `@Autowired`.
2. In the test method, create a workflow stub
   using `workflowClient.newWorkflowStub()`
   with `WorkflowOptions` that specify the
   task queue.
3. Execute the workflow by calling
   `sayHello("Temporal")`.
4. Assert the result equals the expected
   greeting.

The real `HelloActivityImpl` (a Spring
`@Component`) is auto-discovered and
registered — no mocking needed.

### Step 3 — Write the Mockito-based test

Open `HelloWorkflowMockitoTest.java`. This
test uses `@MockitoBean` to replace the real
activity bean with a Mockito mock, giving you
control over the activity's behavior and
verification.

1. Inject the `WorkflowClient` using
   `@Autowired`.
2. Declare a `@MockitoBean` field for
   `HelloActivityImpl`:

```java
@MockitoBean
private HelloActivityImpl mockedActivity;
```

`@MockitoBean` replaces the real Spring bean
with a Mockito mock in the application
context. The Temporal auto-configuration
picks up this mock when registering
activities.

3. In the `setUp()` method, configure the
   mock's behavior:

```java
when(mockedActivity.greet(anyString()))
    .thenAnswer(inv ->
        "Mocked: Hello, " + inv.getArgument(0)
            + "!");
```

4. In the test method, execute the workflow,
   assert the mocked result, and use
   `verify()` to confirm the activity was
   called:

```java
verify(mockedActivity).greet("Temporal");
```

`@MockitoBean` is preferable when you need to
verify call counts, capture arguments, or set
up complex stubbing logic.

### Step 4 — Write the replay test

Open `HelloWorkflowReplayTest.java`. Replay
testing verifies that changes to workflow code
are backwards-compatible with executions that
are already running. It works by replaying a
recorded workflow event history against the
current workflow implementation. If the code
has changed in a non-deterministic way (e.g.
reordering activities, changing arguments),
the replay fails.

In this test you will run the workflow once to
produce a real history, then replay that
history against the current code.

1. Create a workflow stub and execute
   `sayHello("Temporal")`.
2. Fetch the recorded execution history:

```java
WorkflowExecution execution = WorkflowStub
    .fromTyped(workflow).getExecution();
var history = workflowClient.fetchHistory(
    execution.getWorkflowId());
```

3. Replay the history against the current
   implementation:

```java
WorkflowReplayer.replayWorkflowExecution(
    history, HelloWorkflowImpl.class);
```

When this call succeeds, it means your current
workflow code is compatible with the recorded
execution. When it throws, you have introduced
a non-deterministic change that would break
in-flight workflows.

> **Tip:** In production you would typically
> export histories from the Temporal Web UI or
> CLI (`temporal workflow show --output json`)
> and replay them from JSON files using
> `WorkflowReplayer`
> `.replayWorkflowExecutionFromResource()`.
> The programmatic approach used here is
> self-contained and does not require a
> running server.

### Step 5 — Run the tests

Run all three tests:

```bash
./mvnw test
```

All three tests should pass. No running
Temporal server is needed — the test
environment handles everything in-memory.

## Key Takeaways

- `@SpringBootTest` with the Temporal test
  server provides an in-memory Temporal
  service for fast, isolated testing.

- Use `@ActiveProfiles("test")` with an
  `application-test.yaml` to enable the test
  server without overriding the main
  configuration.

- Workflows and activities are
  auto-discovered and registered just like in
  production — no manual worker setup.

- Activities can be mocked with
  `@MockitoBean` to isolate workflow logic.

- Replay tests catch non-deterministic
  workflow changes before they break
  production.

- Use `Workflow.getLogger()` in workflows
  even in tests — the test environment
  handles replay-safe logging.

## Resources

- [Testing — Temporal Java SDK](https://docs.temporal.io/develop/java/testing-suite)

## Solution

A working solution is available in the
`solution/` directory.
