package io.temporal.app.domain.workflows.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.app.domain.integrations.HelloActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.springframework.context.annotation.Profile;

import java.time.Duration;


@WorkflowImpl(taskQueues = "HelloSampleTaskQueue")
public class HelloWorldWorkflowImpl implements HelloWorldWorkflow {

    private final HelloActivity helloActivity = Workflow.newActivityStub(
            HelloActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    @Override
    public String sayHello(Name name) {
        return helloActivity.greet(name);
    }
}
