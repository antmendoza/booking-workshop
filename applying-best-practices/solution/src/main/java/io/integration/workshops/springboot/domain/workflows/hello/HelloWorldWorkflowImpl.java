package io.integration.workshops.springboot.domain.workflows.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.integration.workshops.springboot.domain.workflows.activities.HelloActivity;
import org.springframework.context.annotation.Profile;

import java.time.Duration;


@Profile(value = "hello")
@WorkflowImpl(taskQueues = "HelloSampleTaskQueue")
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
