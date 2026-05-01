package io.temporal.workshops.springboot.interceptor.metric;

import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "interceptor-metric")
@ActivityImpl(taskQueues = "HelloSampleInterceptor")
public class HelloActivityInterceptorImpl implements HelloActivityInterceptor {


    @Override
    public String greet(String name) {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        if (attempt < 6) {
            throw new RuntimeException("Attempt ["+attempt+"], Simulating a transient failure");
        }
        return "Hello, " + name + "!";
    }
}
