package io.temporal.workshops.springboot.testing.hello;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.testing.WorkflowReplayer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HelloWorkflowReplayTest {

    @Autowired
    private WorkflowClient workflowClient;

    @Test
    void replayFromHistory() throws Exception {
        // Run the workflow once to produce a real history.
        var workflow = workflowClient.newWorkflowStub(
                HelloWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId("testing-hello-replay")
                        .setTaskQueue(HelloWorkflow.TASK_QUEUE)
                        .build());

        workflow.sayHello("Temporal");

        // Fetch the recorded execution history.
        WorkflowExecution execution = WorkflowStub
                .fromTyped(workflow).getExecution();
        var history = workflowClient.fetchHistory(
                execution.getWorkflowId());

        // Replay the history against the current
        // implementation. If the workflow code changed in a
        // non-deterministic way, this call will throw.
        WorkflowReplayer.replayWorkflowExecution(
                history, HelloWorkflowImpl.class);
    }
}
