package io.temporal.workshops.springboot.integration.hello;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

// @WorkflowInterface marks this as a Temporal workflow contract.
// The interface must be public so that workflow starters (clients) can reference it,
// but the implementation class can remain package-private.
@WorkflowInterface
public interface HelloWorkflow {

    // Task queue name shared between the workflow, its activities, and the starter.
    // This is the named channel that routes work to the correct worker process.
    String TASK_QUEUE = "HelloTaskQueue";

    // @WorkflowMethod defines the entry point for this workflow.
    // A workflow interface must have exactly one @WorkflowMethod.
    @WorkflowMethod
    String sayHello(String name);
}
