package io.temporal.workshops.springboot.testing.hello;

import io.temporal.testing.WorkflowReplayer;
import org.junit.jupiter.api.Test;

class HelloWorkflowReplayFromFileTest {

    @Test
    void replayFromJsonFile() throws Exception {
        // Replay a workflow execution history exported
        // from the Temporal CLI or Web UI. The file
        // was generated with:
        //   temporal workflow show \
        //       -w <workflowId> --output json \
        //       > hello-workflow-history.json
        WorkflowReplayer.replayWorkflowExecutionFromResource(
                "hello-workflow-history.json",
                HelloWorkflowImpl.class);
    }
}
