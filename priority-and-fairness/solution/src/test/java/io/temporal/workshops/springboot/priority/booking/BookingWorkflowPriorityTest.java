package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

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

        // Start 2 workflows per priority level (6 total)
        for (int priority : priorities) {
            for (int i = 1; i <= 2; i++) {
                String bookingId = "P%d-%03d".formatted(priority, i);
                String hotelName = "Hotel-P" + priority;

                var workflow = workflowClient.newWorkflowStub(
                        BookingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(BookingWorkflow.TASK_QUEUE)
                                .setWorkflowId("priority-test-" + bookingId)
                                .setPriority(Priority.newBuilder()
                                        .setPriorityKey(priority)
                                        .setFairnessKey(hotelName)
                                        .setFairnessWeight(1.0f)
                                        .build())
                                .build());

                var request = new BookingRequest(bookingId, "Guest " + i, hotelName, priority);
                WorkflowClient.start(workflow::processBooking, request);

                stubs.add(WorkflowStub.fromTyped(workflow));
            }
        }

        // Collect results from all workflows
        for (WorkflowStub stub : stubs) {
            String result = stub.getResult(String.class);
            assertNotNull(result, "Expected a confirmation string from workflow " + stub.getExecution().getWorkflowId());
        }
    }
}
