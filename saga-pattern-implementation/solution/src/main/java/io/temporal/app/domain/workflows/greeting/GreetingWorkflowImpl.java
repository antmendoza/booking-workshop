package io.temporal.app.domain.workflows.greeting;

import io.temporal.activity.ActivityOptions;
import io.temporal.app.domain.integrations.GreetingActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Saga;
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
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());
        try {
            String result1 = greetingActivity.greet1(name);
            saga.addCompensation(greetingActivity::compensate1, name);

            Name name2 = new Name();
            name2.setFirstName(name.getFirstName());
            name2.setLastName(name.getLastName() + "-1");
            String result2 = greetingActivity.greet2(name2);
            saga.addCompensation(greetingActivity::compensate2, name2);

            Name name3 = new Name();
            name3.setFirstName(name.getFirstName());
            name3.setLastName(name2.getLastName() + "-2");
            return greetingActivity.greet3(name3);

        } catch (ActivityFailure e) {
            // Compensate as needed
            saga.compensate();
            // throw exception - still marking WF as failed.
            // For other implementations we may choose to not fail WF
            // but perform other actions and complete WF successfully.
            throw e;
        }
    }
}
