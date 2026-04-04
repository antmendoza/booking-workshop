package io.temporal.workflow.interceptor;

import io.nexusrpc.handler.OperationContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.NexusOperationInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "workflow-interceptor-metric")
public class RetryLoggingWorkerInterceptor implements WorkerInterceptor {

    @Override
    public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
        return next;
    }

    @Override
    public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
        return new RetryLoggingActivityInterceptor(next);
    }

    @Override
    public NexusOperationInboundCallsInterceptor interceptNexusOperation(
            OperationContext context, NexusOperationInboundCallsInterceptor next) {
        return next;
    }
}