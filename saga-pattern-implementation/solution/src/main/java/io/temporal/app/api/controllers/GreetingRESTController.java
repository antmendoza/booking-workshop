package io.temporal.app.api.controllers;

import io.temporal.app.domain.messages.Name;
import io.temporal.app.domain.workflows.greeting.GreetingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class GreetingRESTController {

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    GreetingRESTController(WorkflowClient workflowClient,
                           @Value("${spring.temporal.workers[0].task-queue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    @PostMapping("/hello")
    String greet(@RequestBody Name name) {
        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId("greeting-" + UUID.randomUUID())
                .build();

        var workflow = workflowClient.newWorkflowStub(GreetingWorkflow.class, options);
        return workflow.sayHello(name);
    }
}
