package io.temporal.app.domain.workflows.hello;

import io.temporal.app.domain.messages.Name;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloWorldWorkflow {

    // Default number of loop iterations in the main workflow body.
    // Each iteration sleeps for 20 seconds, giving ~5 minutes total runtime.
    int DEFAULT_ITERATIONS = 12;

    @WorkflowMethod
    String sayHello(Name name);

    // Returns the greeting from one activity execution immediately.
    // Used with update-with-start so the caller gets a result without
    // waiting for the full workflow loop to complete.
    @UpdateMethod
    String greet(Name name);
}
