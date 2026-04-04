package io.temporal.workflow.interceptor.localactivity.auth;

import io.temporal.activity.Activity;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "interceptor-localactivity-auth")
@ActivityImpl(taskQueues = "HelloSampleInterceptor")
public class HelloActivityInterceptorImpl implements HelloActivityInterceptor {


    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
