package io.temporal.workshops.springboot.testing.hello;

import io.temporal.testing.WorkflowReplayer;
import org.junit.jupiter.api.Test;

class HelloWorkflowReplayFromFileTest {

    // This test replays a workflow execution from a
    // JSON history file. In production, you would
    // export the history using the Temporal CLI:
    //   temporal workflow show \
    //       -w <workflowId> --output json \
    //       > hello-workflow-history.json
    // The file is already provided in
    // src/test/resources/.

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
