package io.temporal.workshops.springboot.testing.hello;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.WorkflowReplayer;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.Test;

class HelloWorkflowReplayTest {

    @Test
    void replayFromHistory() throws Exception {
        // Run the workflow once to produce a real history.
        try (TestWorkflowEnvironment testEnv =
                     TestWorkflowEnvironment.newInstance()) {

            Worker worker =
                    testEnv.newWorker(HelloWorkflow.TASK_QUEUE);
            worker.registerWorkflowImplementationTypes(
                    HelloWorkflowImpl.class);
            worker.registerActivitiesImplementations(
                    (HelloActivity) name -> "Hello, " + name + "!");
            testEnv.start();

            WorkflowClient client = testEnv.getWorkflowClient();
            HelloWorkflow workflow = client.newWorkflowStub(
                    HelloWorkflow.class,
                    WorkflowOptions.newBuilder()
                            .setTaskQueue(HelloWorkflow.TASK_QUEUE)
                            .build());

            workflow.sayHello("Temporal");

            // Fetch the recorded execution history.
            WorkflowExecution execution = WorkflowStub
                    .fromTyped(workflow).getExecution();
            var history = client.fetchHistory(
                    execution.getWorkflowId());

            // Replay the history against the current
            // implementation. If the workflow code changed in a
            // non-deterministic way, this call will throw.
            WorkflowReplayer.replayWorkflowExecution(
                    history, HelloWorkflowImpl.class);
        }
    }
}
