package io.temporal.workshops.springboot.domain.workflows.hello;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "hello")
public class StarterRunner implements ApplicationRunner {


    private static final Logger log = LoggerFactory.getLogger(StarterRunner.class);
    private static final String TASK_QUEUE = "HelloSampleTaskQueue";

    private final WorkflowClient workflowClient;

    public StarterRunner(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        HelloWorldWorkflow workflow = workflowClient.newWorkflowStub(
                HelloWorldWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("hello-" + System.currentTimeMillis())
                        .build()
        );

        WorkflowExecution execution = WorkflowClient.start(workflow::sayHello, "Temporal");
        log.info("Workflow started with workflowId [{}]", execution.getWorkflowId());
    }
}
