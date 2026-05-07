package io.temporal.app.domain.workflows.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
import io.temporal.app.domain.integrations.HelloActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
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
    // TODO: Add @WorkflowVersioningBehavior(VersioningBehavior.PINNED) here.
    //       Import io.temporal.common.VersioningBehavior
    //       and io.temporal.workflow.WorkflowVersioningBehavior.
    public String sayHello(Name name) {
        var buildId = "";

        String result = null;
        for (var i = 0; i < DEFAULT_ITERATIONS; i++) {
            var iteration = i + 1;
            buildId = helloLocalActivity.getWorkerVersion();
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
