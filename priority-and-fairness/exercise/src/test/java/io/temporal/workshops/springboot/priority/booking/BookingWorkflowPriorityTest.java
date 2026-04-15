package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Demonstrates that workflows with different priority levels all complete successfully.
 * In a real environment with load, higher-priority workflows would be scheduled first.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingWorkflowPriorityTest {

    @Autowired
    private WorkflowClient workflowClient;

    @Test
    void workflowsWithDifferentPriorities_allComplete() {
        int[] priorities = {1, 3, 5};
        List<WorkflowStub> stubs = new ArrayList<>();

        // TODO: For each priority level (1, 3, 5),
        //       start 2 workflows asynchronously using
        //       WorkflowClient.start().
        //
        //       For each workflow:
        //       1. Generate a bookingId like
        //          "P%d-%03d".formatted(priority, i)
        //       2. Set a hotelName like "Hotel-P" + priority
        //       3. Build WorkflowOptions with:
        //          - Task queue: BookingWorkflow.TASK_QUEUE
        //          - Workflow ID: "priority-test-" + bookingId
        //          - Priority with the current priority
        //            level, hotelName as fairness key,
        //            and weight 1.0f
        //       4. Start the workflow asynchronously with
        //          WorkflowClient.start(workflow::processBooking, request)
        //       5. Add WorkflowStub.fromTyped(workflow)
        //          to the stubs list

        // Verify workflows were started
        assertFalse(stubs.isEmpty(),
                "TODO: start workflows above");

        // Collect results from all workflows
        for (WorkflowStub stub : stubs) {
            String result = stub.getResult(String.class);
            assertNotNull(result,
                    "Expected a confirmation string from workflow "
                            + stub.getExecution().getWorkflowId());
        }
    }
}
