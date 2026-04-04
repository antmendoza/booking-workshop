package io.temporal.workflow.interceptor.localactivity.auth;

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
@Profile(value = "interceptor-localactivity-auth")
public class StarterRunner implements ApplicationRunner {


    private static final Logger log = LoggerFactory.getLogger(StarterRunner.class);
    private static final String TASK_QUEUE = "HelloSampleInterceptor";

    private final WorkflowClient workflowClient;

    public StarterRunner(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @Override
    public void run(ApplicationArguments args) {

        String profile = System.getProperty("spring.profiles.active");

        HelloInterceptorWorkflow workflow = workflowClient.newWorkflowStub(
                HelloInterceptorWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)



                        .setWorkflowId(profile + System.currentTimeMillis())
                        .build()
        );

        WorkflowExecution execution = WorkflowClient.start(workflow::sayHello, "Temporal");
        log.info("Workflow started with workflowId [{}]", execution.getWorkflowId());
    }
}
