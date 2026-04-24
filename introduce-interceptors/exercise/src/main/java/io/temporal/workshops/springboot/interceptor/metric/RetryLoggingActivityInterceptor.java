package io.temporal.workshops.springboot.interceptor.metric;

import io.temporal.activity.ActivityExecutionContext;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryLoggingActivityInterceptor extends ActivityInboundCallsInterceptorBase {

    private static final Logger log = LoggerFactory.getLogger(RetryLoggingActivityInterceptor.class);

    private ActivityExecutionContext context;

    public RetryLoggingActivityInterceptor(ActivityInboundCallsInterceptor next) {
        super(next);
    }

    @Override
    public void init(ActivityExecutionContext context) {
        this.context = context;
        super.init(context);
    }

    @Override
    public ActivityOutput execute(ActivityInput input) {
        //TODO follow the README to implement this logic

        return super.execute(input);
    }
}
