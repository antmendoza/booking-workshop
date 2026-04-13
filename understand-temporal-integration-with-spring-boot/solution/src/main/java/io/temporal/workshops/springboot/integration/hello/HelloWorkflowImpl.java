package io.temporal.workshops.springboot.integration.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

// @WorkflowImpl is a Spring Boot auto-discovery annotation from the Temporal Spring Boot starter.
// It registers this implementation with a worker polling the specified task queue — no manual
// worker setup needed. Without this annotation, you would have to create a WorkerFactory and
// register the workflow type yourself.
//
// Notice there is NO @Component here. Temporal creates a new workflow instance for each execution
// (and again on replay), so the SDK — not Spring — owns the lifecycle. Making this a Spring bean
// would produce a single shared instance, breaking Temporal's per-execution isolation.
// Activities are different: they are stateless, so @Component (singleton scope) is fine and even
// recommended — it lets Spring inject dependencies like repositories or REST clients.
@WorkflowImpl(taskQueues = HelloWorkflow.TASK_QUEUE)
class HelloWorkflowImpl implements HelloWorkflow {

    // Workflow code is replayed from event history on recovery. Standard loggers would emit
    // duplicate log entries during replay. Workflow.getLogger() suppresses logs on replay,
    // producing output only on the first (real) execution.
    private static final Logger LOGGER = Workflow.getLogger(HelloWorkflowImpl.class);

    // Workflow.newActivityStub() returns a proxy that does NOT call the activity directly.
    // Instead, each method call schedules an ActivityTask on the Temporal server, which a
    // worker then picks up and executes. This decoupling is what gives Temporal its
    // durability guarantees.
    private final HelloActivity helloActivity = Workflow.newActivityStub(
            HelloActivity.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build()
    );

    @Override
    public String sayHello(String name) {
        LOGGER.info("Saying hello: {}", name);
        return helloActivity.greet(name);
    }
}
