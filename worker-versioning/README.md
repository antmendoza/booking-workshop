# Worker Versioning in Temporal

Temporal's **Worker Versioning** feature lets you
deploy new Workflow code safely without breaking
Workflow Executions that are already running. You
tag each Worker build with a **Build ID**, group
Workers under a **Worker Deployment**, and let
Temporal route Workflow Tasks to the right
version.

Each Workflow type declares a **Versioning
Behavior**:

- **Pinned** — the execution runs to completion
  on the Worker Deployment Version it started on.
  You can change Worker code freely; already-running
  Workflows are never affected. Ideal for
  short-running Workflows.
- **Auto-Upgrade** — the execution migrates to the
  Current Deployment Version on its next Workflow
  Task. Ideal for long-running Workflows, but
  requires you to keep new code replay-compatible
  with old histories (or use Patching).

In this exercise you will configure a Spring Boot
application to run as a versioned Temporal Worker,
annotate two booking Workflows with their
Versioning Behaviors, and then watch what happens
as you roll out a new Worker Deployment Version
against running Workflows.

## Objective

- Configure a Worker with `WorkerDeploymentOptions`
  (Build ID, Deployment Name, `useVersioning`)
  through a `TemporalOptionsCustomizer` bean
- Annotate a Workflow implementation with
  `@WorkflowVersioningBehavior(PINNED)`
- Annotate another Workflow implementation with
  `@WorkflowVersioningBehavior(AUTO_UPGRADE)`
- Run two Worker versions (`v1.0`, `v2.0`) against
  a real Temporal server and use the Temporal CLI
  to switch the Current Version
- Observe pinned Workflows completing on their
  original version while auto-upgrade Workflows
  migrate to the newer version

## Prerequisites

- Java 21
- [Temporal CLI](https://docs.temporal.io/cli)
  version 1.4.1 or later
- Temporal Server 1.29.1 or later (the
  `start-dev` server bundled with a recent CLI
  is fine — Worker Deployments are enabled by
  default from 1.28+)
- Familiarity with Temporal workflows, activities,
  and signals
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

> **Note:** Worker Versioning in this exercise
> uses the GA Worker Deployments API. If your
> Temporal Server is older than 1.28, enable the
> feature with
> `--dynamic-config-value system.enableDeployments=true
> --dynamic-config-value system.enableDeploymentVersions=true`.

## Key Concepts

### Worker Deployment and Deployment Version

A **Worker Deployment** is a logical service
(e.g. `booking-workers`). It has a **Current
Version** and optionally a **Ramping Version**.

A **Worker Deployment Version** is identified by
a `(deploymentName, buildId)` pair — for example,
`booking-workers / 1.0`. A single Worker process
belongs to exactly one Deployment Version.

### Versioning Behavior

Workflows that run on a versioned Worker must
declare how they respond to new deployments:

```text
PINNED        -> completes on its original
                 Deployment Version. Never
                 affected by newer code.
AUTO_UPGRADE  -> moves to the Current Deployment
                 Version on its next Workflow
                 Task. Must stay replay-safe
                 across versions.
```

### Java API

Opt a Worker into versioning with
`WorkerDeploymentOptions`:

```java
WorkerOptions.newBuilder()
    .setDeploymentOptions(
        WorkerDeploymentOptions.newBuilder()
            .setVersion(new WorkerDeploymentVersion(
                "booking-workers", "1.0"))
            .setUseVersioning(true)
            .build())
    .build();
```

Annotate each Workflow implementation with its
behavior:

```java
@Override
@WorkflowVersioningBehavior(
    VersioningBehavior.PINNED)
public String processBooking(
        BookingRequest request) {
    ...
}
```

The Spring Boot starter builds `WorkerOptions`
internally, so you hook into it with a bean that
implements
`TemporalOptionsCustomizer<WorkerOptions.Builder>`.

### Rolling out a new version

Once both v1 and v2 Workers are running, use the
Temporal CLI to promote v2:

```bash
temporal worker deployment set-current-version \
    --deployment-name booking-workers \
    --build-id 2.0
```

From that moment:

- New Workflow Executions start on v2.
- `AUTO_UPGRADE` Workflows already running on v1
  migrate to v2 on their next Workflow Task.
- `PINNED` Workflows already running on v1 stay
  on v1 forever.

## Steps

### Step 1 — Explore the project

Open the `exercise/` directory. A Spring Boot
application is already wired up in the
`versioning.booking` package:

- **`BookingRequest`** — a record with
  `bookingId`, `customerName`, `hotelName`
- **`BookingActivity`** — a single
  `confirmBooking` method that returns a string
  including the Worker's Build ID (read from
  `app.versioning.build-id`)
- **`PinnedBookingWorkflow`** — waits for an
  `approve` signal, then calls `confirmBooking`
- **`AutoUpgradeBookingWorkflow`** — identical
  shape
- **`BookingService`** — starts workflows and
  sends the `approve` signal
- **`BookingController`** — REST endpoints to
  start workflows, approve them, and fetch
  results

The main configuration lives in
`application.yaml` and picks up three env vars:

```yaml
app:
  versioning:
    enabled: ${WORKER_VERSIONING_ENABLED:false}
    deployment-name: ${TEMPORAL_DEPLOYMENT_NAME:booking-workers}
    build-id: ${TEMPORAL_BUILD_ID:1.0}
```

Two profile files flip versioning on and pick
distinct Build IDs:

- `application-v1.yaml` — `build-id: "1.0"`
- `application-v2.yaml` — `build-id: "2.0"`

Activate them with
`-Dspring-boot.run.profiles=v1` or `v2`.

### Step 2 — Wire the Worker to opt into versioning

Open `TemporalVersioningConfig.java`. The
`workerVersioningCustomizer` bean currently
returns a pass-through customizer — the Worker
is unversioned no matter what you set in
`application.yaml`.

Find the two TODOs in the bean method and
implement the logic:

1. If `properties.enabled()` is `false`, log
   `"Worker versioning disabled"` and return a
   pass-through customizer
   (`optionsBuilder -> optionsBuilder`).
2. Otherwise, log
   `"Worker versioning enabled: deployment={},
   buildId={}"` and return a customizer that
   calls:

   ```java
   optionsBuilder.setDeploymentOptions(
       WorkerDeploymentOptions.newBuilder()
           .setVersion(new WorkerDeploymentVersion(
               properties.deploymentName(),
               properties.buildId()))
           .setUseVersioning(true)
           .build());
   ```

Add the required imports:

```java
import io.temporal.common.WorkerDeploymentVersion;
import io.temporal.worker.WorkerDeploymentOptions;
```

### Step 3 — Declare each Workflow's behavior

When a Worker opts into versioning, every
registered Workflow type must declare its
Versioning Behavior. If it does not, the Worker
fails at registration time.

Open `PinnedBookingWorkflowImpl.java` and add
the annotation to `processBooking`:

```java
import io.temporal.common.VersioningBehavior;
import io.temporal.workflow.WorkflowVersioningBehavior;

@Override
@WorkflowVersioningBehavior(
    VersioningBehavior.PINNED)
public String processBooking(
        BookingRequest request) { ... }
```

Do the same for
`AutoUpgradeBookingWorkflowImpl.java`, but use
`VersioningBehavior.AUTO_UPGRADE`.

### Step 4 — Verify with the unit tests

Open `BookingControllerTest.java` and implement
the two test methods. Each test should:

1. POST a `BookingRequest` JSON to the start
   endpoint (`/bookings/pinned` or
   `/bookings/auto-upgrade`) and read the
   `workflowId` from the response body.
2. POST `/bookings/{workflowId}/approve` to
   unblock the workflow.
3. GET `/bookings/{workflowId}/result` and
   assert the response body contains
   `"confirmed (mocked)"`.
4. `verify(mockedActivity).confirmBooking(any())`
   on the mock.

Run the tests:

```bash
./mvnw test
```

The test profile (`application-test.yaml`) sets
`app.versioning.enabled: false`, so the
annotations are ignored and the in-memory test
server runs both workflows as unversioned. This
verifies your code compiles and the signal
mechanics work. Versioning behavior itself only
shows up against a real Temporal server, which
you will do next.

### Step 5 — Run the v1 Worker against a real server

Start the Temporal dev server (Step 0) in one
terminal, then in another terminal start the v1
Worker:

```bash
./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=v1
```

You should see a log line:

```text
Worker versioning enabled:
  deployment=booking-workers, buildId=1.0
```

Promote v1 to the Current Version for the
deployment (required before any versioned
Workflow can start):

```bash
temporal worker deployment set-current-version \
    --deployment-name booking-workers \
    --build-id 1.0 \
    --yes
```

Check the deployment:

```bash
temporal worker deployment describe \
    --name booking-workers
```

You should see `CurrentVersion` set to
`booking-workers.1.0`.

Start a pinned booking and an auto-upgrade
booking — both will start on v1:

```bash
curl -X POST http://localhost:8080/bookings/pinned \
     -H 'Content-Type: application/json' \
     -d '{"bookingId":"BK-001",
          "customerName":"Alice",
          "hotelName":"Luxury Resort"}'

curl -X POST \
     http://localhost:8080/bookings/auto-upgrade \
     -H 'Content-Type: application/json' \
     -d '{"bookingId":"BK-002",
          "customerName":"Bob",
          "hotelName":"City Hotel"}'
```

Both workflows are now blocked on their `approve`
signal. In the Temporal Web UI, each execution
shows a Versioning Info block with
`Version: booking-workers.1.0` and the expected
`Behavior: Pinned` or `AutoUpgrade`.

### Step 6 — Deploy v2 and promote it

Leave the v1 Worker running. In a new terminal,
start a v2 Worker on a different HTTP port:

```bash
SERVER_PORT=8081 \
    ./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=v2
```

The log should now say `buildId=2.0`. Both
Workers are polling the same Task Queue, but
each only accepts tasks routed to its Deployment
Version.

Promote v2 to current:

```bash
temporal worker deployment set-current-version \
    --deployment-name booking-workers \
    --build-id 2.0 \
    --yes
```

### Step 7 — Approve and observe

Approve both workflows and fetch their results:

```bash
curl -X POST \
    http://localhost:8080/bookings/pinned-BK-001/approve
curl -X POST \
    http://localhost:8080/bookings/auto-upgrade-BK-002/approve

curl http://localhost:8080/bookings/pinned-BK-001/result
curl http://localhost:8080/bookings/auto-upgrade-BK-002/result
```

Watch what each workflow returns:

- **Pinned (`BK-001`)** completes on v1:

  ```text
  Booking BK-001 confirmed for Alice at
  Luxury Resort — served by v1.0
  ```

- **Auto-upgrade (`BK-002`)** migrated to v2
  on its next Workflow Task (the one triggered
  by the signal) and runs the activity on v2:

  ```text
  Booking BK-002 confirmed for Bob at
  City Hotel — served by v2.0
  ```

This is the whole point of Worker Versioning:
long-running or existing executions can be left
alone (pinned) or can be safely carried into the
new code (auto-upgrade), without any patching
branches inside the Workflow definition.

### Step 8 — Try a fresh pair on v2

With v2 as the Current Version, start two more
bookings:

```bash
curl -X POST http://localhost:8080/bookings/pinned \
     -H 'Content-Type: application/json' \
     -d '{"bookingId":"BK-003",
          "customerName":"Chloe",
          "hotelName":"Budget Inn"}'

curl -X POST \
     http://localhost:8080/bookings/auto-upgrade \
     -H 'Content-Type: application/json' \
     -d '{"bookingId":"BK-004",
          "customerName":"Dan",
          "hotelName":"City Hotel"}'
```

Both start on v2. Approve and fetch their
results — both mention `served by v2.0`, because
v2 is where they began. Pinning and auto-upgrade
only differ when the Current Version changes
mid-flight.

## Key Takeaways

1. **Worker Versioning** routes Workflow Tasks to
   Worker builds by `(deploymentName, buildId)`.
   Workers opt in via `WorkerDeploymentOptions`
   with `useVersioning=true`.
2. **Versioning Behavior** is declared per
   Workflow type with
   `@WorkflowVersioningBehavior(...)`.
3. **Pinned** executions complete on their
   original Deployment Version — no patching
   ever required.
4. **Auto-Upgrade** executions migrate to the
   Current Version on their next Workflow Task —
   useful for long-running Workflows, but you
   must keep the new code replay-compatible
   (patching still applies on breaking changes).
5. Use the Temporal CLI
   (`temporal worker deployment
   set-current-version`) to roll forward, roll
   back, or ramp between Deployment Versions
   without restarting the Workers.
6. In Spring Boot, customise `WorkerOptions` via
   a `TemporalOptionsCustomizer` bean; the
   autoconfigure module picks it up automatically.

## Resources

- [Worker Versioning — Temporal Docs](https://docs.temporal.io/production-deployment/worker-deployments/worker-versioning)
- [Versioning Behaviors — Worker Versioning](https://docs.temporal.io/worker-versioning#versioning-behaviors)
- [temporal worker deployment — CLI reference](https://docs.temporal.io/cli/worker#deployment)
- [Spring Boot integration — Java SDK](https://docs.temporal.io/develop/java/spring-boot-integration)
- [Worker Versioning sample — samples-java](https://github.com/temporalio/samples-java/tree/main/core/src/main/java/io/temporal/samples/workerversioning)

## Solution

A working solution is available in the
`solution/` directory.
