package io.temporal.workshops.springboot.testing.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

@WorkflowImpl(taskQueues = HelloWorkflow.TASK_QUEUE)
public class HelloWorkflowImpl implements HelloWorkflow {

    private static final Logger LOGGER =
            Workflow.getLogger(HelloWorkflowImpl.class);

    private final HelloActivity helloActivity =
            Workflow.newActivityStub(
                    HelloActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(10))
                            .build());

    @Override
    public String sayHello(String name) {
        LOGGER.info("Saying hello: {}", name);
        return helloActivity.greet(name);
    }
}
