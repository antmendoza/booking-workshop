# Worker Versioning

Temporal's **Worker Versioning** feature lets you deploy new
Worker code without disrupting Workflow Executions that are
already running. Each Worker registers with a
**Deployment Name** and **Build ID**. Temporal routes Workflow
Tasks to the right version automatically.

Each Workflow type must declare how it should behave when a
newer Deployment Version is promoted:

- **PINNED** — the execution completes on the Deployment
  Version it started on. Ideal for short-lived Workflows.
- **AUTO_UPGRADE** — the execution migrates to the Current
  Version on its next Workflow Task. Useful for long-running
  Workflows, but requires replay-safe code changes.

## How the app works

The exercise starts from the **Applying Best Practices**
project structure. The `HelloWorldWorkflow` has been extended
for this exercise:

- `sayHello(Name)` — the `@WorkflowMethod`. Runs a loop that
  calls the hello activity `DEFAULT_ITERATIONS` (12) times
  with a 20-second sleep between each call, giving a total
  runtime of approximately 5 minutes. Each activity call sets
  a human-readable summary on the activity options showing the
  current iteration and worker build ID, visible in the
  Temporal Web UI.  This has been done to make the workflow 
  "long running" meaning version changes can be observed.
- `greet(Name)` — an `@UpdateMethod`. Calls the activity once
  and returns the greeting immediately. Used with
  **update-with-start** in the REST controller so that
  `POST /hello` returns a result straight away while
  `sayHello` continues running in the background.


## Prerequisites

- Java 21
- [Temporal CLI](https://docs.temporal.io/cli) 1.4.1 or later
- Temporal Server 1.29.1 or later (the `start-dev` server
  bundled with a recent CLI works — Worker Deployments are
  enabled by default from 1.28+)

> **Note:** If your server is older than 1.28 add:
> `--dynamic-config-value system.enableDeployments=true`
> `--dynamic-config-value system.enableDeploymentVersions=true`

Start a local Temporal dev server:

```bash
temporal server start-dev
```

The server listens on `127.0.0.1:7233` and the Web UI is at
`http://localhost:8233`.


## Steps

### Step 1 — Explore the exercise

Open the `exercise/` directory. The key files to understand
before making any changes:

The key files that will need to be changed are:-

**`application-vN.yaml`** — the `deoloyment-version` section
controls whether the worker opts into versioning and with
which Build ID:

```yaml
spring:
  temporal:
    workers:
      -  task-queue: HelloSampleTaskQueue
         deployment-properties:
           deployment-version: ${TEMPORAL_DEPLOYMENT_NAME:hello-workers}.${TEMPORAL_BUILD_ID:1.0}
           use-versioning: true
           default-versioning-behaviour: "PINNED"
```



**`HelloWorldWorkflowImpl.java`** — `sayHello` is missing its
versioning behavior annotation:  Again this has been commented out so uncomment to
enable the versioning behaviour for the Hello workflow.

```java
@Override
// TODO: Add @WorkflowVersioningBehavior(VersioningBehavior.PINNED)
public String sayHello(Name name) { ... }
```

Run the app to ensure it behaves as expected prior to versioning being implemented.
```bash
./mvnw spring-boot:run -f ./pom.xml
```

Call the endpoint:

```bash
curl -s -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan"}'
```

Expected: `Hello, Xiao Zhan! (served by -)`

If you then look at the Temporal UI you will see the response came from an update command and then the workflow 
is still running to iterate round the same activity multiple times.  It can run in the background while you 
move on to make code changes to implement versioning.


### Step 2 — Implement versioning 


Open `HelloWorldWorkflowImpl.java`. Add the annotation to the
`sayHello` method:

```java
import io.temporal.common.VersioningBehavior;
import io.temporal.workflow.WorkflowVersioningBehavior;

@Override
@WorkflowVersioningBehavior(VersioningBehavior.PINNED)
public String sayHello(Name name) { ... }
```

`PINNED` tells Temporal that this Workflow Execution must
complete on the Deployment Version it started on. It will
never be migrated to a newer version mid-flight.


Edit both of the application-vN.yaml files to uncomment the configuration that will specify
the version of the worker.  We can then run different instances with different profiles to 
show how workers startup running with versioning.

### Step 4 — Verify with the unit tests

Run the tests:

```bash
./mvnw test
```

The test profile (`application-test.yaml`) sets
`app.versioning.enabled: false`, so the annotation is ignored
and all tests run against the in-memory test server.

### Step 5 — Run the v1 Worker against a real server

Start the Temporal dev server, then in a second terminal start
the v1 Worker:

```bash
./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=v1
```

At this point you can try and start a workflow.  
```bash
curl -s -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan"}'
```
However the curl command will not return and if you look at the Temporal UI you can see the 
worflow running but not progressing.  This is because while we have workers running there is no current 
version set so no workers are at the correct version to progress the Worflow.

The commandline can be used to query the status of the deployments.

```bash
temporal worker deployment describe --name="hello-workers"
```
This produces output along the lines of

```
Worker Deployment:
Name                  hello-workers
CreateTime            21 hours ago
LastModifierIdentity  temporal-cli:donald@tannoch.local

Version Summaries:
DeploymentName  BuildID  DrainageStatus   CreateTime
hello-workers   1.0      unspecified     21 hours ago
Promote v1 to the Current Version of the deployment:
```
in the woker deployment section there is nothing indicating the current version.  This means no workers
will be connected to progress a workflow.
If we set the current version then the workflow will start progressing, the update used to start the workflow
will complete.

```bash
temporal worker deployment set-current-version \
    --deployment-name hello-workers \
    --build-id 1.0 \
    --yes
```

Confirm the deployment is registered:

```bash
temporal worker deployment describe \
    --name hello-workers
```
Check the workflow you started earlier is in progress now.

### Step 6 — Deploy the v2 Worker

Leave the v1 Worker running. In a new terminal, start a v2
Worker, the v2 application yaml file specifies to use a different port from the v1 version so the
application should startup cleanly.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=v2
```
At this point v1.0 should be the current version and v2.0 is running.  Start a workflow via the REST endpoint
```bash
curl -s -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan-v1"}'
```
You should see the v1 worker picking this up and starting to process the workflow.  (You can stop the v1 worker
to pause processing or ensure you do the next steps within the lifetime of the workflow running)

Promote v2 to the Current Version:

```bash
temporal worker deployment set-current-version \
    --deployment-name hello-workers \
    --build-id 2.0 \
    --yes
```

### Step 7 — Observe version routing

Start another workflow running 

```bash
curl -s -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan-v2"}'
```

With v2 as the Current Version, new workflow executions start
on v2. Start another workflow:

Meanwhile, the workflow that started on v1 is still
running. Open the Web UI and compare the two executions the 
search attribute `Deployment Version` shows the worker that will
run the workflow and inside the workflow the summary also shows the
worker version that acts on a given activity.

This is the PINNED guarantee: The initial workflow is completely
unaffected by the promotion of v2.

Now rollback to the v1 again using the temporal command line to specify 
the current version is v1.0 again.

# Step 8 - Manually upgrading a workflow
It is possible to override the version of a workflow to force it onto a more recent version.
This can be done using the command line.
Start a workflow
```bash
curl -s -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan"}'
```
This should start as a V1 worker.  Watch to ensure the first activities are run as v1. 
Just to give plenty of time to see the behaviour at this point stop your V1 worker.

Now start your v2 worker if not already running.
Set the current version to v2

```bash
temporal worker deployment set-current-version \
    --deployment-name hello-workers \
    --build-id 2.0 \
    --yes
```
We will need the workflow ID of the running instance so in the UI copy the ID 
from the screen ready to use in the command to upgrade. 
Now restart your V1 worker and watch an iteration/activity proceed with it remaining on the v1 worker.

Change its version to V2.  Change the WORKFLOW_ID in the below command to be the workflow ID you copied from the UI.

```bash
temporal workflow update-options \
  --workflow-id "WORKFLOW_ID" \
  --versioning-override-behavior pinned \
  --versioning-override-deployment-name "hello-workers" \
  --versioning-override-build-id "2.0"
```
You should be able to watch the workflow progressing on screen and switching from the V1 worker to the V2.


# Step 9 - try out Unpinned workflows.
In this case we will change the workflow from PINNED to AUTO_UPGRADE and then 
start the workflow with one worker version and deploy a new version of the worker
which will then take over running the workflow.

Firstly ensure the current version is set to version 1.

```bash
temporal worker deployment set-current-version --deployment-name="hello-workers" --build-id 1.0
```

Edit the HelloWorldWorkflowImpl class to change the annotation to specifiy the workflow is auto-upgrade
```aiignore
 @Override
    @WorkflowVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
    public String sayHello(Name name) {

```
Start up both V1 and V2 workers in different terminal windows.

```bash
mvn spring-boot:run -f ./pom.xml -Dspring-boot.run.profiles=v1
```
```bash
mvn spring-boot:run -f ./pom.xml -Dspring-boot.run.profiles=v2
```

Start a workflow running and observe that it is on V1 in the UI.
```bash
curl -X POST http://localhost:3030/hello \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Xiao","lastName":"Zhan"}'
```

Issue the command to set the V2 version as the current version 
```bash
temporal worker deployment set-current-version --deployment-name="hello-workers" --build-id 2.0 --yes
```

Observe that the next activity to run will be routed to the v2 worker.


## Key Takeaways

1. Workers opt into versioning via `WorkerDeploymentOptions`
   with `useVersioning=true`. In Spring Boot this is done with configuration.
2. Workflow type registered on a versioned Worker can declare
   its versioning behavior with
   `@WorkflowVersioningBehavior(...)`.
3. `PINNED` executions complete on their original Deployment
   Version — no code patching is needed for short-lived
   Workflows.
4. `AUTO_UPGRADE` executions migrate to the Current Version on
   their next Workflow Task. Code changes must remain
   replay-safe (or use Patching for breaking changes).
5. Use `temporal worker deployment set-current-version` to
   roll forward, roll back, or ramp traffic between versions
   without restarting Workers.

## Resources

- [Worker Versioning — Temporal Docs](https://docs.temporal.io/production-deployment/worker-deployments/worker-versioning)
- [Versioning Behaviors](https://docs.temporal.io/worker-versioning#versioning-behaviors)
- [temporal worker deployment CLI reference](https://docs.temporal.io/cli/worker#deployment)
- [Spring Boot integration — Java SDK](https://docs.temporal.io/develop/java/spring-boot-integration)

## Solution

A completed implementation is in the `solution/` directory.
