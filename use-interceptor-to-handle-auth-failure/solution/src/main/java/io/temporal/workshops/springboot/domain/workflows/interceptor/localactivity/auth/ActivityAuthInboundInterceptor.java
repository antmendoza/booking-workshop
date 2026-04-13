package io.temporal.workshops.springboot.domain.workflows.interceptor.localactivity.auth;

import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;

public class ActivityAuthInboundInterceptor extends WorkflowInboundCallsInterceptorBase {

    public ActivityAuthInboundInterceptor(WorkflowInboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
        // Wrap the outbound interceptor so executeActivity calls are intercepted
        super.init(new ActivityAuthOutboundInterceptor(outboundCalls));
    }
}
