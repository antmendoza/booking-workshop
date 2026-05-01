package io.temporal.app.domain.workflows.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.app.domain.integrations.HelloActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.common.VersioningBehavior;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.common.SearchAttributeKey;
import io.temporal.common.SearchAttributes;
import io.temporal.workflow.WorkflowVersioningBehavior;

import org.slf4j.Logger;

import java.time.Duration;

@WorkflowImpl(taskQueues = "HelloSampleTaskQueue")
public class HelloWorldWorkflowImpl implements HelloWorldWorkflow {

    private static final Logger LOGGER =
            Workflow.getLogger(HelloWorldWorkflowImpl.class);

    private final HelloActivity helloActivity = Workflow.newActivityStub(
            HelloActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );
    private final HelloActivity helloLocalActivity = Workflow.newLocalActivityStub(
            HelloActivity.class,
            LocalActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(3))
                    .build()
    );

    @Override
    // Change annotation to AUTO_UPGRADE for last step in exercise.
    // @WorkflowVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
    @WorkflowVersioningBehavior(VersioningBehavior.PINNED)
    public String sayHello(Name name) {
        String buildId = helloLocalActivity.getWorkerVersion();

        String result = null;
        for (var i = 0; i < DEFAULT_ITERATIONS; i++) {
            var iteration = i + 1;
            buildId = helloLocalActivity.getWorkerVersion();
            System.out.println("BuildId: " + buildId);

            var summary = "Iteration " + iteration + "/" + DEFAULT_ITERATIONS
                    + " (" + buildId + ")";
            var stub = Workflow.newActivityStub(
                    HelloActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(10))
                            .setSummary(summary)
                            .build()
            );
            result = stub.greet(name);
            LOGGER.info("Iteration {}/{}: {}", iteration, DEFAULT_ITERATIONS, result);
            if (i < DEFAULT_ITERATIONS - 1) {
                Workflow.sleep(Duration.ofSeconds(20));
            }
        }
        return result;
    }

    @Override
    public String greet(Name name) {
        return helloActivity.greet(name);
    }
}
