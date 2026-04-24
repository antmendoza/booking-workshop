package io.temporal.workshops.springboot.interceptor.localactivity.auth;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface HelloActivityInterceptor {

    String one(String name);

    String two(String name);

    String three(String name);

    String regenerateAuthToken();
}
