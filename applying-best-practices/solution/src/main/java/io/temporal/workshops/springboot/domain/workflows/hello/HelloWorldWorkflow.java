package io.temporal.workshops.springboot.domain.workflows.hello;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloWorldWorkflow {

    @WorkflowMethod
    String sayHello(String name);
}
