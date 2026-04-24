package io.temporal.app.api.controllers;

import java.util.UUID;

import io.temporal.app.domain.messages.Name;
import io.temporal.app.domain.workflows.hello.HelloWorldWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloRESTController {

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    HelloRESTController(WorkflowClient workflowClient,
                        @Value("${spring.temporal.workers[0].task-queue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    @PostMapping("/hello")
    String hello(@RequestBody Name name) {
        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId("hello-" + UUID.randomUUID())
                .build();

        var workflow = workflowClient.newWorkflowStub(HelloWorldWorkflow.class, options);
        return workflow.sayHello(name);
    }
}