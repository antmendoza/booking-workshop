package io.temporal.workflow.interceptor.localactivity.auth;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HelloActivityInterceptor {

    String one(String name);

    String two(String name);

    String three(String name);

    String regenerateAuthToken();
}
