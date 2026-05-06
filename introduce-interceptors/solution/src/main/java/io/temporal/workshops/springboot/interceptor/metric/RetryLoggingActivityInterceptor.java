package io.temporal.workshops.springboot.interceptor.metric;

import com.uber.m3.tally.Scope;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
        ActivityInfo info = context.getInfo();
        int attempt = info.getAttempt();

        //emit metric only every 5th attempt
        if (attempt % 5 == 0) {

            Scope scope = context.getMetricsScope()
                    .tagged(Map.of(
                            // heads up, this can create metrics with very high cardinality
                            // setting a tag with the workflow run id for demonstration purposes
                            "workflow_run_id", info.getRunId()
                    ));

            scope.counter("activity_retry").inc(5);

        }

        return super.execute(input);
    }
}
