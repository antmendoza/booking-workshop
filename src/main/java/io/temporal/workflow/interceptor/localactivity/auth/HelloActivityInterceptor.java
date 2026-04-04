package io.temporal.workflow.interceptor.localactivity.auth;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HelloActivityInterceptor {

    @ActivityMethod
    String greet(String name);
}
