package io.temporal.workshops.springboot.workertuning;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.workshops.springboot.workertuning.worker.HelloWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static io.temporal.workshops.springboot.workertuning.worker.HelloWorkflow.TASK_QUEUE;

@Component
@Profile("starter")
public class StarterRunner implements ApplicationRunner {


    private static final Logger log = LoggerFactory.getLogger(StarterRunner.class);

    private final WorkflowClient workflowClient;

    public StarterRunner(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public void run(ApplicationArguments args) {


        for (int i = 0; i < 100; ++i) {
            HelloWorkflow workflow = workflowClient.newWorkflowStub(
                    HelloWorkflow.class,
                    WorkflowOptions.newBuilder()
                            .setTaskQueue(TASK_QUEUE)
                            .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_TERMINATE_EXISTING)
                            .setWorkflowId("hello-world-" + i)
                            .build()
            );
            WorkflowExecution execution = WorkflowClient.start(workflow::sayHello, "Temporal");
            log.info("Workflow started with workflowId [{}]", execution.getWorkflowId());

        }


        System.exit(0);

    }
}
