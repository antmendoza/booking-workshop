package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
class BookingWorkflowTest {

    @Autowired
    private WorkflowClient workflowClient;

    @Test
    void processBooking_returnsConfirmation() {
        // TODO: Create a workflow stub for BookingWorkflow
        //       with WorkflowOptions that include:
        //       - Task queue: BookingWorkflow.TASK_QUEUE
        //       - Priority with priorityKey=1,
        //         fairnessKey="test-hotel",
        //         fairnessWeight=1.0f
        //
        // Hint: Use Priority.newBuilder() to build
        //       the priority, then pass it to
        //       WorkflowOptions via .setPriority(...)
        //
        // Import: io.temporal.common.Priority

        // TODO: Create a BookingRequest with:
        //       bookingId="TEST-001",
        //       customerName="Alice",
        //       hotelName="test-hotel",
        //       priority=1

        // TODO: Call workflow.processBooking(request)
        //       and assert the result equals:
        //       "Booking TEST-001 confirmed for Alice at test-hotel"

        fail("TODO: implement this test");
    }
}
