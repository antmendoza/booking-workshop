package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class BookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingService.class);

    private final WorkflowClient workflowClient;

    BookingService(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    WorkflowStub submitPinned(BookingRequest request) {
        var workflowId = "pinned-" + request.bookingId();
        var workflow = workflowClient.newWorkflowStub(
                PinnedBookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(PinnedBookingWorkflow.TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .build());

        WorkflowClient.start(workflow::processBooking, request);
        LOGGER.info("Started pinned workflow {}", workflowId);
        return WorkflowStub.fromTyped(workflow);
    }

    WorkflowStub submitAutoUpgrade(BookingRequest request) {
        var workflowId = "auto-upgrade-" + request.bookingId();
        var workflow = workflowClient.newWorkflowStub(
                AutoUpgradeBookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(AutoUpgradeBookingWorkflow.TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .build());

        WorkflowClient.start(workflow::processBooking, request);
        LOGGER.info("Started auto-upgrade workflow {}", workflowId);
        return WorkflowStub.fromTyped(workflow);
    }

    void approve(String workflowId) {
        LOGGER.info("Approving workflow {}", workflowId);
        workflowClient.newUntypedWorkflowStub(workflowId).signal("approve");
    }
}
