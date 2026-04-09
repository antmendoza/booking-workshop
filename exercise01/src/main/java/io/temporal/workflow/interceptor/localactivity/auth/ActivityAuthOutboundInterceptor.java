package io.temporal.workflow.interceptor.localactivity.auth;

import io.temporal.activity.LocalActivityOptions;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptorBase;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;

public class ActivityAuthOutboundInterceptor extends WorkflowOutboundCallsInterceptorBase {

    private static final Logger log = LoggerFactory.getLogger(ActivityAuthOutboundInterceptor.class);


    private final HelloActivityInterceptor myActivities = Workflow.newLocalActivityStub(
            HelloActivityInterceptor.class,
            LocalActivityOptions.newBuilder()
                    .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                    .build());


    public ActivityAuthOutboundInterceptor(WorkflowOutboundCallsInterceptor next) {
        super(next);
    }


    @Override
    public <R> ActivityOutput<R> executeActivity(ActivityInput<R> input) {
        log.info("Intercepted executeActivity call: activityName={}", input.getActivityName());
        ActivityOutput<R> rActivityOutput = null;
        try {

            rActivityOutput = super.executeActivity(input);

            //block until activity completes or fails
            rActivityOutput.getResult().get();

        } catch (ActivityFailure e) {

            if (((ApplicationFailure) e.getCause()).getType().equals("TokenExpired")) {
                log.error("Error executing activity doctor: {}", e.getMessage());

                // generate a new token
                String newToken = myActivities.regenerateAuthToken();
                MDC.put("x-auth-jwt-token", newToken);

                // retry the original activity
                rActivityOutput = super.executeActivity(input);

            }
        }

        return rActivityOutput;
    }
}
