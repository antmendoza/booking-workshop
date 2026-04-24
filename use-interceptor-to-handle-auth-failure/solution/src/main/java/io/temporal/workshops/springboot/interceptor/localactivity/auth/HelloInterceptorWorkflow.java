package io.temporal.workshops.springboot.interceptor.localactivity.auth;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloInterceptorWorkflow {

    @WorkflowMethod
    String sayHello(String name);
}
