package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class BookingWorkflowTest {

    @Autowired
    private WorkflowClient workflowClient;

    @Test
    void processBooking_returnsConfirmation() {
        var workflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(BookingWorkflow.TASK_QUEUE)
                        .setPriority(Priority.newBuilder()
                                .setPriorityKey(1)
                                .setFairnessKey("test-hotel")
                                .setFairnessWeight(1.0f)
                                .build())
                        .build());

        var request = new BookingRequest("TEST-001", "Alice", "test-hotel", 1);
        var result = workflow.processBooking(request);

        assertEquals("Booking TEST-001 confirmed for Alice at test-hotel", result);
    }
}
