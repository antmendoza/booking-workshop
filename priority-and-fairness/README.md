# Priority and Fairness in Temporal Task Queues

Temporal lets you control the execution order
of workflows and activities within a single
task queue using **priority** and **fairness**.

**Priority** assigns an integer key (1 to 5,
lower = higher priority) to each task. When a
backlog forms, tasks are dispatched strictly in
priority order: all priority-1 tasks run before
priority-3, which run before priority-5.

**Fairness** adds a string key and a float
weight to distribute work across logical
groups (tenants, tiers, applications) within
the same priority level. The system dispatches
tasks in proportion to their weights, preventing
any single group from monopolising workers.

In this exercise you will configure a booking
workflow to use both features, observe how
priority affects processing order, and see how
fairness prevents starvation across tenants.

## Objective

- Set priority keys on `WorkflowOptions` to
  control execution order
- Set fairness keys and weights to balance
  work across tenants
- Observe priority ordering and fair scheduling
  through worker logs
- Write tests that exercise workflows with
  different priority levels

## Prerequisites

- Java 21
- Temporal CLI (`temporal server start-dev`)
- Familiarity with Temporal workflows and
  activities (interfaces, implementations,
  task queues)
- Familiarity with Spring Boot basics

## Key Concepts

### Priority Key

A positive integer from 1 to 5. Lower values
mean higher priority. The default is 3.

```
Priority 1  ->  Highest (urgent / VIP)
Priority 3  ->  Normal  (default)
Priority 5  ->  Lowest  (batch / idle)
```

When tasks are backlogged, all priority-1
tasks are dispatched before any priority-3
task, and all priority-3 before any priority-5.
Tasks at the same priority level follow FIFO
order.

### Fairness Key and Weight

A short string (up to 64 bytes) that groups
tasks into virtual queues — typically a tenant
ID, a tier name, or an application identifier.
Each group gets dispatched in proportion to its
weight (default 1.0). For example, with two
groups at weight 1.0 each, both get roughly
equal throughput regardless of backlog size.

Fairness operates **within** a priority level.
Priority is checked first, then fairness
distributes work among groups at the same
priority.

### Java API

```java
import io.temporal.common.Priority;

WorkflowOptions options = WorkflowOptions.newBuilder()
    .setTaskQueue("my-queue")
    .setPriority(Priority.newBuilder()
        .setPriorityKey(1)
        .setFairnessKey("tenant-a")
        .setFairnessWeight(2.0f)
        .build())
    .build();
```

Activities and child workflows inherit priority
from their parent workflow by default, but can
override it.

## Steps

### Step 1 — Explore the project

Open the `exercise/` directory. A working
Spring Boot application is provided with a
`BookingWorkflow` and `BookingActivity` in the
`booking` package.

Review the domain model:

- **`BookingRequest`** — a record with
  `bookingId`, `customerName`, `hotelName`
  (tenant), and `priority` (1–5)
- **`BookingWorkflow`** — orchestrates the
  booking by calling `validateBooking` then
  `confirmBooking`
- **`BookingActivity`** — validates and
  confirms bookings with a small delay to
  make ordering visible
- **`BookingController`** — REST endpoint
  that submits a batch of 15 bookings

Look at `application.yaml` — the worker is
configured to process **one activity at a
time**:

```yaml
spring:
  temporal:
    workers:
      - task-queue: BookingTaskQueue
        capacity:
          max-concurrent-activity-executors: 1
```

This is essential: priority only affects tasks
that are **backlogged**. With the default
concurrency (200), all 15 bookings would be
processed instantly with no visible ordering.
Limiting to 1 forces a queue where priority
ordering becomes obvious.

The `POST /bookings/start` endpoint submits
15 workflows across three tiers:

| Tenant        | Prefix | Priority | Count |
|---------------|--------|----------|-------|
| Luxury Resort | LR-    | 1 (VIP)  | 5     |
| City Hotel    | CH-    | 3        | 5     |
| Budget Inn    | BI-    | 5 (low)  | 5     |

### Step 2 — Add priority to the controller

Open `BookingController.java`. The
`submitBooking` method creates
`WorkflowOptions` but does not yet set a
priority. Find the TODO and add the priority
configuration:

1. Import `io.temporal.common.Priority`
2. Build a `Priority` with:
   - `priorityKey` from `request.priority()`
   - `fairnessKey` from `request.hotelName()`
   - `fairnessWeight` of `1.0f`
3. Pass it to `WorkflowOptions` using
   `.setPriority(...)`

```java
.setPriority(Priority.newBuilder()
        .setPriorityKey(request.priority())
        .setFairnessKey(request.hotelName())
        .setFairnessWeight(1.0f)
        .build())
```

### Step 3 — Run and observe priority ordering

Start the Temporal dev server with the
priority-aware task dispatcher enabled:

```bash
temporal server start-dev \
    --dynamic-config-value \
    matching.useNewMatcher=true
```

Without this flag, tasks are dispatched in
FIFO order regardless of priority settings.

In a separate terminal, start the application:

```bash
./mvnw spring-boot:run
```

Then trigger the booking batch:

```bash
curl -XPOST http://localhost:8080/bookings/start
```

Watch the application logs. Because the worker
is limited to one concurrent activity, a
backlog forms and priority ordering becomes
visible. Each log line shows the priority
level:

```
[priority=1] Validating booking LR-001 ...
[priority=1] Confirming booking LR-001 ...
[priority=1] Validating booking LR-002 ...
...
[priority=3] Validating booking CH-001 ...
...
[priority=5] Validating booking BI-001 ...
```

You should see all priority-1 bookings
(Luxury Resort) processed first, then
priority-3 (City Hotel), then priority-5
(Budget Inn).

> **Note:** You may notice a low-priority
> booking slip through early in the output.
> This is expected — priority only affects
> tasks that are **backlogged**. The very
> first tasks are dispatched before the
> backlog forms, so they bypass priority
> ordering. Once the queue builds up, strict
> priority takes over.

The 500ms validation delay combined with the
single-activity concurrency limit creates a
clear sequential execution where the priority
ordering is unmistakable.

### Step 4 — Observe fairness within a priority

Now let's see fairness in action. Edit
`BookingController.java` to submit all 15
bookings at the **same** priority level (3),
but keep the different `fairnessKey` values
(`"Luxury Resort"`, `"City Hotel"`,
`"Budget Inn"`):

```java
.setPriority(Priority.newBuilder()
        .setPriorityKey(3)
        .setFairnessKey(request.hotelName())
        .setFairnessWeight(1.0f)
        .build())
```

Restart the application and trigger the batch
again with `curl`. With equal weights, each
tenant gets roughly equal dispatch throughput
— you should see bookings from all three
hotels interleaved, rather than one hotel's
entire backlog processed before the next.

Try changing the weight for one tenant (e.g.
`"Luxury Resort"` at weight 3.0) and observe
that it gets dispatched proportionally more
often.

### Step 5 — Write the integration test

Open `BookingWorkflowTest.java`. The test
class is already set up with `@SpringBootTest`
and `@ActiveProfiles("test")`.

1. Create a `WorkflowOptions` with task queue
   `BookingWorkflow.TASK_QUEUE` and a
   `Priority` (priorityKey=1,
   fairnessKey="test-hotel",
   fairnessWeight=1.0f)
2. Create a `BookingRequest` with
   bookingId="TEST-001", customerName="Alice",
   hotelName="test-hotel", priority=1
3. Call `workflow.processBooking(request)` and
   assert the result equals
   `"Booking TEST-001 confirmed for Alice at test-hotel"`

### Step 6 — Write the multi-priority test

Open `BookingWorkflowPriorityTest.java`. This
test starts workflows at different priority
levels and verifies they all complete.

For each priority level (1, 3, 5), start 2
workflows asynchronously:

1. Build `WorkflowOptions` with the priority
   key, a fairness key based on the hotel name,
   and a unique workflow ID
2. Use `WorkflowClient.start(workflow::processBooking, request)`
   for async execution
3. Track each `WorkflowStub` in a list
4. After starting all 6 workflows, iterate
   through the stubs and call
   `stub.getResult(String.class)` to verify
   each one completed

### Step 7 — Run the tests

```bash
./mvnw test
```

Both tests should pass. The test environment
uses the in-memory Temporal test server — no
running Temporal server is needed.

## Key Takeaways

- **Priority key** (1–5) controls strict
  execution order within a task queue. Lower
  numbers run first. Default is 3.

- **Fairness key** groups tasks into virtual
  queues with round-robin dispatch proportional
  to their weight. It prevents one tenant or
  workload type from starving others.

- Priority is checked first, then fairness
  distributes work within the same priority
  level.

- Activities and child workflows **inherit**
  priority from their parent workflow unless
  explicitly overridden.

- Both features work on a **single task queue**
  — no need to create separate queues per
  priority or tenant.

- Priority and fairness require the new
  task dispatcher, enabled with
  `matching.useNewMatcher=true` on the
  Temporal server.

## Resources

- [Task Queue Priority and Fairness — Temporal Docs](https://docs.temporal.io/develop/task-queue-priority-fairness)
- [Priority API — Java SDK](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/Priority.html)
- [Temporal Master Class: Priority Task Queues (video)](https://www.youtube.com/watch?v=Nc8d8cNlEwc)

## Solution

A working solution is available in the
`solution/` directory.
