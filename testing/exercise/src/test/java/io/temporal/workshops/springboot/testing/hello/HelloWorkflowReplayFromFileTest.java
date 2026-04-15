package io.temporal.workshops.springboot.testing.hello;

import io.temporal.testing.WorkflowReplayer;
import org.junit.jupiter.api.Test;

class HelloWorkflowReplayFromFileTest {

    // Export history with: temporal workflow show -w <workflowId> --output json

    @Test
    void replayFromJsonFile() throws Exception {
        // TODO: Replay the history against the current
        //       implementation using:
        //         WorkflowReplayer
        //             .replayWorkflowExecutionFromResource(
        //                 "hello-workflow-history.json",
        //                 HelloWorkflowImpl.class)
        //       If the workflow code changed in a
        //       non-deterministic way, this call will
        //       throw.
    }
}
