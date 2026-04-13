package io.integration.workshops.springboot.domain.workflows.interceptor.localactivity.auth;

import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "interceptor-localactivity-auth")
@ActivityImpl(taskQueues = "HelloInterceptorLocalactivityAuth")
public class HelloActivityInterceptorImpl implements HelloActivityInterceptor {


    @Override
    public String one(String name) {

        anyHttpRequest();
        return "Hello, " + name + "!";
    }

    @Override
    public String two(String name) {

        anyHttpRequest();
        return "Hello, " + name + "!";
    }

    @Override
    public String three(String name) {

        anyHttpRequest();
        return "Hello, " + name + "!";
    }

    @Override
    public String regenerateAuthToken() {
        return "new-valid-token";
    }

    private void anyHttpRequest() {
        //simulate a http request
        String token = MDC.get("x-auth-jwt-token");
        if(token.equals("expired-token")) {
            throw ApplicationFailure.newNonRetryableFailure("Token is expired", "TokenExpired");
        }
    }
}
