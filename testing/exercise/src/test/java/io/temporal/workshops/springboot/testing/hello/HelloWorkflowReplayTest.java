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

    // TODO: Inject the WorkflowClient using @Autowired.

    @Test
    void replayFromHistory() throws Exception {
        // TODO: Run the workflow once to produce a real
        //       history.
        //       1. Create a workflow stub for
        //          HelloWorkflow with the correct task
        //          queue.
        //       2. Execute sayHello("Temporal").

        // TODO: Fetch the recorded execution history.
        //       1. Get the WorkflowExecution from the
        //          stub:
        //            WorkflowStub.fromTyped(workflow)
        //                .getExecution()
        //       2. Fetch the history:
        //            workflowClient.fetchHistory(
        //                execution.getWorkflowId())

        // TODO: Replay the history against the current
        //       implementation using:
        //         WorkflowReplayer
        //             .replayWorkflowExecution(
        //                 history,
        //                 HelloWorkflowImpl.class)
        //       If the workflow code changed in a
        //       non-deterministic way, this call will
        //       throw.
    }
}
