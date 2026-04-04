package io.temporal.workflow.interceptor.localactivity.auth;

import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivityAuthOutboundInterceptor extends WorkflowOutboundCallsInterceptorBase {

    private static final Logger log = LoggerFactory.getLogger(ActivityAuthOutboundInterceptor.class);

    public ActivityAuthOutboundInterceptor(WorkflowOutboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public <R> ActivityOutput<R> executeActivity(ActivityInput<R> input) {
        log.info("Intercepted executeActivity call: activityName={}", input.getActivityName());
        ActivityOutput<R> rActivityOutput = null;
        try {


            rActivityOutput = super.executeActivity(input);
        } catch (Exception e) {


        }


        return rActivityOutput;
    }
}
