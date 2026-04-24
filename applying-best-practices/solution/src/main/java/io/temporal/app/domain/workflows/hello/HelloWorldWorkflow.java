package io.temporal.app.domain.workflows.hello;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.app.domain.messages.Name;

@WorkflowInterface
public interface HelloWorldWorkflow {

    @WorkflowMethod
    String sayHello(Name name);
}
