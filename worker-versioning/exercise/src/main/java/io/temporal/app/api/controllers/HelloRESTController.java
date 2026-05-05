package io.temporal.app.api.controllers;

import io.temporal.app.domain.messages.Name;
import io.temporal.app.domain.workflows.hello.HelloWorldWorkflow;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.client.UpdateOptions;
import io.temporal.client.WithStartWorkflowOperation;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowUpdateStage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class HelloRESTController {

    private final WorkflowClient workflowClient;
    private final String taskQueue;

    HelloRESTController(
            WorkflowClient workflowClient,
            @Value("${spring.temporal.workers[0].task-queue}") String taskQueue) {
        this.workflowClient = workflowClient;
        this.taskQueue = taskQueue;
    }

    @PostMapping("/hello")
    String hello(@RequestBody Name name) {
        var options = WorkflowOptions.newBuilder()
                .setTaskQueue(taskQueue)
                .setWorkflowId("hello-" + UUID.randomUUID())
                .setWorkflowIdConflictPolicy(
                        WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_USE_EXISTING)
                .build();

        var workflow = workflowClient.newWorkflowStub(
                HelloWorldWorkflow.class, options);

        var startOp = new WithStartWorkflowOperation<>(workflow::sayHello, name);

        var updateOptions = UpdateOptions.newBuilder(String.class)
                .setWaitForStage(WorkflowUpdateStage.COMPLETED)
                .build();

        return WorkflowClient.executeUpdateWithStart(
                workflow::greet, name, updateOptions, startOp);
    }
}
