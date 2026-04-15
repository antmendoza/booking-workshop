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
- Observe priority ordering through worker logs
- Observe fair-share interleaving across tenants
  at the same priority level
- Observe proportional dispatch with unequal
  fairness weights

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

```text
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

The controller also exposes two additional
endpoints that you will use in later steps:

- `POST /bookings/start-fairness` — submits
  the same 15 bookings, all at priority 3,
  with equal fairness weights (1.0)
- `POST /bookings/start-weighted` — submits
  the same 15 bookings, all at priority 3,
  with Luxury Resort at weight 3.0 and the
  other two hotels at weight 1.0

### Step 2 — Add priority to the controller

Open `BookingController.java`. The
`submitBooking` method creates
`WorkflowOptions` but does not yet set a
priority. The method signature is
`submitBooking(request, idPrefix,
fairnessWeight)` — the `fairnessWeight`
parameter is already wired in from each
endpoint. Find the TODO and add the priority
configuration:

1. Import `io.temporal.common.Priority`
2. Build a `Priority` with:
   - `priorityKey` from `request.priority()`
   - `fairnessKey` from `request.hotelName()`
   - `fairnessWeight` from the method parameter
3. Pass it to `WorkflowOptions` using
   `.setPriority(...)`

```java
.setPriority(Priority.newBuilder()
        .setPriorityKey(request.priority())
        .setFairnessKey(request.hotelName())
        .setFairnessWeight(fairnessWeight)
        .build())
```

Note that `fairnessWeight` comes from the
method parameter, not a hardcoded value. This
lets each endpoint pass a different weight
without duplicating the workflow submission
logic.

### Step 3 — Run and observe priority ordering

Start the Temporal dev server with the
priority-aware task dispatcher enabled:

```bash
temporal server start-dev \
    --dynamic-config-value \
    matching.useNewMatcher=true \
    --dynamic-config-value \
    matching.enableFairness=true \
    --dynamic-config-value \
    matching.enableMigration=true
```

The `matching.useNewMatcher` flag enables the
new task dispatcher required for priority
ordering (Step 3). The `matching.enableFairness`
and `matching.enableMigration` flags enable
fair-share scheduling across tenants (Step 4).
Without these flags, tasks are dispatched in
FIFO order regardless of priority and fairness
settings.

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

### Step 4 — Observe fair-share interleaving

Now that the `Priority` is wired into
`submitBooking`, the fairness endpoint is
ready to use. Trigger it:

```bash
curl -XPOST \
    http://localhost:8080/bookings/start-fairness
```

This endpoint submits the same 15 bookings but
**all at priority 3**. With equal fairness
weights (1.0) and different fairness keys (one
per hotel name), the Temporal task dispatcher
distributes work proportionally across the
three hotels.

Watch the application logs. Instead of one
hotel's entire backlog being processed before
the next, you should see bookings from all
three hotels **interleave** in the completion
stream.

Contrast this with Step 3: without fairness
(different priorities), all priority-1 bookings
ran first. With fairness (same priority), the
dispatcher round-robins across hotels so that
no single tenant monopolises the worker.

> **Note:** Fairness distributes work
> proportionally — it does not guarantee
> strict alternation. With equal weights you
> should see roughly equal throughput across
> the three hotels, but the exact interleaving
> may vary between runs.

### Step 5 — Observe weighted fairness

Trigger the weighted endpoint:

```bash
curl -XPOST \
    http://localhost:8080/bookings/start-weighted
```

This endpoint gives Luxury Resort a weight of
3.0 while City Hotel and Budget Inn stay at
1.0. The dispatcher should give Luxury Resort
roughly 3x the throughput.

Watch the logs: Luxury Resort bookings appear
more frequently in the completion stream —
roughly 3 out of every 5 completions should
belong to Luxury Resort.

> **Note:** The weighting is approximate,
> especially with small batch sizes. With 15
> bookings and a single-activity worker, the
> proportional effect is visible but not
> perfectly 3:1 on every run.

### Step 6 — Restart and test all three endpoints

For a clean comparison of the three behaviours
side by side, restart the application and run
each endpoint in sequence:

1. Priority ordering:

   ```bash
   curl -XPOST \
       http://localhost:8080/bookings/start
   ```

2. Fair-share interleaving:

   ```bash
   curl -XPOST \
       http://localhost:8080/bookings/start-fairness
   ```

3. Weighted dispatch:

   ```bash
   curl -XPOST \
       http://localhost:8080/bookings/start-weighted
   ```

> **Tip:** Wait for each batch to complete
> before starting the next, or restart the
> application between runs to avoid workflow
> ID collisions.

## Key Takeaways

1. **Priority key** (1–5) controls strict
   execution order. Lower = higher priority.
   Default is 3.
2. **Fairness key** groups tasks into virtual
   queues. Work is dispatched proportionally
   to weight.
3. Priority is checked first, then fairness
   distributes within the same priority level.
4. Activities and child workflows **inherit**
   priority unless explicitly overridden.
5. Both features work on a **single task
   queue** — no separate queues needed.
6. Priority and fairness require the new task
   dispatcher (`matching.useNewMatcher=true`,
   `matching.enableFairness=true`,
   `matching.enableMigration=true`).

## Resources

- [Task Queue Priority and Fairness — Temporal Docs](https://docs.temporal.io/develop/task-queue-priority-fairness)
- [Priority API — Java SDK](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/Priority.html)
- [Temporal Master Class: Priority Task Queues (video)](https://www.youtube.com/watch?v=Nc8d8cNlEwc)

## Solution

A working solution is available in the
`solution/` directory.
