package io.temporal.workflow.interceptor;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.springframework.context.annotation.Profile;

import java.time.Duration;


@WorkflowImpl(taskQueues = "HelloSampleInterceptor")
@Profile(value = "workflow-interceptor-metric")
public class HelloInterceptorWorkflowImpl implements HelloInterceptorWorkflow {

    private final HelloActivityInterceptor helloActivity = Workflow.newActivityStub(
            HelloActivityInterceptor.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setBackoffCoefficient(1.0) // no backoff
                            .build())
                    .build()
    );

    @Override
    public String sayHello(String name) {
        return helloActivity.greet(name);
    }
}
