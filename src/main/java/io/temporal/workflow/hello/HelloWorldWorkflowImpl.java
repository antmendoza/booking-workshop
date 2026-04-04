package io.temporal.workflow.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.springframework.context.annotation.Profile;

import java.time.Duration;


@WorkflowImpl(taskQueues = "HelloSampleTaskQueue")
@Profile(value = "workflow-hello")
public class HelloWorldWorkflowImpl implements HelloWorldWorkflow {

    private final HelloActivity helloActivity = Workflow.newActivityStub(
            HelloActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public String sayHello(String name) {
        return helloActivity.greet(name);
    }
}
