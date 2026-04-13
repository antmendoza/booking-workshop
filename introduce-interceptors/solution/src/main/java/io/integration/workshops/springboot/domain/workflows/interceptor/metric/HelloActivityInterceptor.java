package io.integration.workshops.springboot.domain.workflows.interceptor.metric;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HelloActivityInterceptor {

    @ActivityMethod
    String greet(String name);
}
