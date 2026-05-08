package io.temporal.app.domain.workflows.greeting;

import io.temporal.app.domain.messages.Name;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingWorkflow {

    @WorkflowMethod
    String sayHello(Name name);
}
