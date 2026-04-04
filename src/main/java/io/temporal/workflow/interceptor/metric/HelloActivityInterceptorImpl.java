package io.temporal.workflow.interceptor.metric;

import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "workflow-interceptor-metric")
@ActivityImpl(taskQueues = "HelloSampleInterceptor")
public class HelloActivityInterceptorImpl implements HelloActivityInterceptor {


    @Override
    public String greet(String name) {
        if (Activity.getExecutionContext().getInfo().getAttempt() < 6) {
            throw new RuntimeException("Simulating a transient failure");
        }
        return "Hello, " + name + "!";
    }
}
