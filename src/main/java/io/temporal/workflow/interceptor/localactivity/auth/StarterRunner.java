package io.temporal.workflow.interceptor.localactivity.auth;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "interceptor-localactivity-auth")
public class StarterRunner implements ApplicationRunner {


    private static final Logger log = LoggerFactory.getLogger(StarterRunner.class);
    private static final String TASK_QUEUE = "HelloInterceptorLocalactivityAuth";

    private final WorkflowClient workflowClient;
  //  private final MDCContextPropagator mdcContextPropagator;

    public StarterRunner(WorkflowClient workflowClient
    //                     MDCContextPropagator mdcContextPropagator
    ) {
        this.workflowClient = workflowClient;
      //  this.mdcContextPropagator = mdcContextPropagator;
    }

    @Override
    public void run(ApplicationArguments args) {

        MDC.put("x-auth-jwt-token", "expired-token");

        HelloInterceptorWorkflow workflow = workflowClient.newWorkflowStub(
                HelloInterceptorWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("interceptor-localactivity-auth" + System.currentTimeMillis())
                        .build()
        );

        WorkflowExecution execution = WorkflowClient.start(workflow::sayHello, "Temporal");
        log.info("Workflow started with workflowId [{}]", execution.getWorkflowId());
    }
}
