package io.temporal.app.domain.workflows.greeting;

import io.temporal.activity.ActivityOptions;
import io.temporal.app.domain.integrations.GreetingActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(taskQueues = "SagaTaskQueue")
public class GreetingWorkflowImpl implements GreetingWorkflow {

    private final ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
            .build();

    private final GreetingActivity greetingActivity =
            Workflow.newActivityStub(GreetingActivity.class, activityOptions);

    @Override
    public String sayHello(Name name) {
        // TODO: wrap steps in a Saga and call saga.compensate() on failure

        String result1 = greetingActivity.greet1(name);

        Name name2 = new Name();
        name2.setFirstName(name.getFirstName());
        name2.setLastName(name.getLastName() + "-1");
        String result2 = greetingActivity.greet2(name2);

        Name name3 = new Name();
        name3.setFirstName(name.getFirstName());
        name3.setLastName(name2.getLastName() + "-2");
        return greetingActivity.greet3(name3);
    }
}
