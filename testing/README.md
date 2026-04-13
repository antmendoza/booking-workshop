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
contains four test files with TODO
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
- **`HelloWorkflowReplayFromFileTest.java`**
  — replays a workflow from a JSON history
  file exported from Temporal

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

### Step 5 — Replay from a JSON history file

Open `HelloWorkflowReplayFromFileTest.java`.
In production, workflow histories are exported
from the Temporal CLI or Web UI and replayed
from JSON files. This avoids needing a running
server to capture histories at test time.

A pre-recorded history file is already
provided at
`src/test/resources/hello-workflow-history.json`.
It was generated with the Temporal CLI:

```bash
temporal workflow show \
    -w <workflowId> --output json \
    > hello-workflow-history.json
```

This test does not use `@SpringBootTest` —
replaying a history file is a pure SDK
operation that only needs the workflow
implementation class.

In the test method, replay the history file
against the current implementation:

```java
WorkflowReplayer
    .replayWorkflowExecutionFromResource(
        "hello-workflow-history.json",
        HelloWorkflowImpl.class);
```

### Step 6 — Run the tests

Run all four tests:

```bash
./mvnw test
```

All four tests should pass. No running
Temporal server is needed — the test
environment handles everything in-memory.

### Step 7 — Break the replay

Now that all tests pass, let's see what
happens when you introduce a
non-deterministic change.

Open `HelloActivity.java` and add a new
method to the activity interface:

```java
@ActivityMethod
int getAge(String name);
```

Open `HelloActivityImpl.java` and implement
it:

```java
@Override
public int getAge(String name) {
    return name.length(); // dummy implementation
}
```

Open `HelloWorkflowImpl.java` and call this
new activity **before** the greeting:

```java
@Override
public String sayHello(String name) {
    LOGGER.info("Saying hello: {}", name);
    int age = helloActivity.getAge(name);
    return helloActivity.greet(name)
        + " You are " + age + ".";
}
```

Run the tests again:

```bash
./mvnw test
```

**What happens?**

- `HelloWorkflowTest` and
  `HelloWorkflowMockitoTest` still pass —
  they run against fresh executions that
  match the updated code.
- `HelloWorkflowReplayTest` still passes —
  it captures a new history and replays it
  immediately, so the history matches the
  current code.
- **`HelloWorkflowReplayFromFileTest` fails**
  with a non-determinism error. The recorded
  history expects a `Greet` activity at
  event 5, but the updated code now
  schedules `GetAge` first. The replayer
  detects the mismatch and throws.

This is exactly the scenario replay testing
is designed to catch: a code change that
would break workflows already running in
production. The JSON history file acts as a
contract — any change that alters the
sequence of commands is flagged before it
reaches production.

Revert your changes to `HelloActivity`,
`HelloActivityImpl`, and
`HelloWorkflowImpl` before continuing.

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
  production. Histories can be replayed
  programmatically or from JSON files
  exported with the Temporal CLI.

- Use `Workflow.getLogger()` in workflows
  even in tests — the test environment
  handles replay-safe logging.

## Resources

- [Testing — Temporal Java SDK](https://docs.temporal.io/develop/java/testing-suite)
- [Testing — Spring Boot Reference](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [@MockitoBean — Spring Framework Reference](https://docs.spring.io/spring-framework/reference/testing/annotations/integration-spring/annotation-mockitobean.html)
- [JUnit Assertions](https://docs.junit.org/6.0.3/writing-tests/assertions.html)

## Solution

A working solution is available in the
`solution/` directory.
