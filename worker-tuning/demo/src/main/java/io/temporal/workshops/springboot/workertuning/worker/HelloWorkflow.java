package io.temporal.workshops.springboot.workertuning.worker;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloWorkflow {

    String TASK_QUEUE = "HelloWorkerTaskQueue";

    @WorkflowMethod
    String sayHello(String name);
}
