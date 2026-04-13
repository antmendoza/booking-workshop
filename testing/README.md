# Testing Temporal Workflows

Temporal provides a dedicated testing framework
(`temporal-testing`) that lets you test workflows
without a running Temporal server. The
`TestWorkflowEnvironment` starts an in-memory
implementation of the Temporal service where you
can register workflows and mock activities.
Additionally, replay testing lets you verify that
code changes are backwards-compatible with
already-running workflow executions.

In this exercise you will write three different
kinds of tests for the Hello workflow: a unit
test with a hand-written activity mock, a
Mockito-based test, and a replay test.

## Objective

Write tests for a Temporal workflow that:

- Use `TestWorkflowEnvironment` for in-memory
  testing
- Mock activities with inline lambdas and with
  Mockito
- Replay a workflow execution from a captured
  history

## Prerequisites

- Java 21
- Familiarity with Temporal workflows and
  activities (interfaces, implementations, task
  queues)
- Familiarity with JUnit 5 basics
- **No running Temporal server needed** -- tests
  use the in-memory test environment

## Steps

### Step 1 -- Explore the project

Open the `exercise/` directory. A working Spring
Boot application is already provided with
`HelloWorkflow` and `HelloActivity` implemented
in the `hello` package.

Look at the `src/test/` directory -- it contains
three test files with TODO instructions:

- **`HelloWorkflowTest.java`** -- tests the
  workflow with a lambda-based activity mock
- **`HelloWorkflowMockitoTest.java`** -- tests
  the workflow using Mockito to mock the activity
- **`HelloWorkflowReplayTest.java`** -- captures
  a workflow history and replays it against the
  current workflow implementation

### Step 2 -- Write the workflow unit test

Open `HelloWorkflowTest.java`. This test uses
`TestWorkflowEnvironment` to run the workflow
in-memory with a hand-written activity mock.

In the `setUp()` method:

1. Create an instance with
   `TestWorkflowEnvironment.newInstance()`.
2. Get a `WorkflowClient` from the test
   environment using `testEnv.getWorkflowClient()`.
3. Create a `Worker` for
   `HelloWorkflow.TASK_QUEUE` using
   `testEnv.newWorker(HelloWorkflow.TASK_QUEUE)`.
4. Register the workflow implementation type on
   the worker with
   `worker.registerWorkflowImplementationTypes(HelloWorkflowImpl.class)`.
5. Register a mocked activity using a lambda
   that implements the `HelloActivity` interface:

```java
worker.registerActivitiesImplementations(
    (HelloActivity) name -> "Hello, " + name + "!"
);
```

6. Start the environment with
   `testEnv.start()`.

In the `tearDown()` method, close the
environment with `testEnv.close()`.

In the test method:

1. Create a workflow stub using
   `client.newWorkflowStub()` with
   `WorkflowOptions` that specify the task
   queue.
2. Execute the workflow by calling
   `sayHello("Temporal")`.
3. Assert the result equals the expected
   greeting.

### Step 3 -- Write the Mockito-based test

Open `HelloWorkflowMockitoTest.java`. This test
uses Mockito to mock the activity, which gives
you more control over verification.

In the `setUp()` method:

1. Create a Mockito mock with
   `Mockito.mock(HelloActivity.class)`.
2. Configure the mock's behavior:

```java
when(mockedActivity.greet(anyString()))
    .thenAnswer(inv ->
        "Mocked: Hello, " + inv.getArgument(0) + "!"
    );
```

3. Create the `TestWorkflowEnvironment`,
   `WorkflowClient`, and `Worker` as in the
   previous test.
4. Register the workflow implementation type.
5. Register the activity by wrapping the mock
   with a lambda. Mockito proxies inherit the
   `@ActivityMethod` annotation, which Temporal
   rejects on concrete classes. A lambda avoids
   this:

```java
worker.registerActivitiesImplementations(
    (HelloActivity) mockedActivity::greet
);
```

6. Start the environment.

In the test method, after executing the workflow
and asserting the result, use `verify()` to
confirm the activity was called with the correct
argument:

```java
verify(mockedActivity).greet("Temporal");
```

Mockito is preferable when you need to verify
call counts, capture arguments, or set up
complex stubbing logic.

### Step 4 -- Write the replay test

Open `HelloWorkflowReplayTest.java`. Replay
testing verifies that changes to workflow code
are backwards-compatible with executions that
are already running. It works by replaying a
recorded workflow event history against the
current workflow implementation. If the code has
changed in a non-deterministic way (e.g.
reordering activities, changing arguments), the
replay fails.

In this test you will run the workflow once to
produce a real history, then replay that history
against the current code.

1. Create a `TestWorkflowEnvironment` using
   try-with-resources so it auto-closes.
2. Create a `Worker`, register
   `HelloWorkflowImpl` and a mocked
   `HelloActivity`, then start the environment.
3. Create a workflow stub, execute
   `sayHello("Temporal")`.
4. Fetch the recorded execution history:

```java
WorkflowExecution execution = WorkflowStub
    .fromTyped(workflow).getExecution();
var history = client.fetchHistory(
    execution.getWorkflowId());
```

5. Replay the history against the current
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
> `WorkflowReplayer.replayWorkflowExecutionFromResource()`.
> The programmatic approach used here is
> self-contained and does not require a running
> server.

### Step 5 -- Run the tests

Run all three tests:

```bash
./mvnw test
```

All three tests should pass. No running Temporal
server is needed -- the test environment handles
everything in-memory.

## Key Takeaways

- `TestWorkflowEnvironment` provides an
  in-memory Temporal service for fast, isolated
  testing.

- Activities can be mocked with simple lambdas
  or Mockito depending on your testing needs.

- Replay tests catch non-deterministic workflow
  changes before they break production.

- Testing does not require a running Temporal
  server.

- Use `Workflow.getLogger()` in workflows even
  in tests -- the test environment handles
  replay-safe logging.

## Resources

- [Testing -- Temporal Java SDK](https://docs.temporal.io/develop/java/testing-suite)

## Solution

A working solution is available in the
`solution/` directory.
