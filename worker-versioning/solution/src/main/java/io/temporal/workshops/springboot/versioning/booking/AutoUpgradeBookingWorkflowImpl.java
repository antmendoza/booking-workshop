package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.VersioningBehavior;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowVersioningBehavior;
import org.slf4j.Logger;

import java.time.Duration;

@WorkflowImpl(taskQueues = AutoUpgradeBookingWorkflow.TASK_QUEUE)
class AutoUpgradeBookingWorkflowImpl implements AutoUpgradeBookingWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(AutoUpgradeBookingWorkflowImpl.class);

    private final BookingActivity bookingActivity = Workflow.newActivityStub(
            BookingActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build());

    private boolean approved;

    @Override
    @WorkflowVersioningBehavior(VersioningBehavior.AUTO_UPGRADE)
    public String processBooking(BookingRequest request) {
        LOGGER.info("Auto-upgrade booking {} waiting for approval", request.bookingId());
        Workflow.await(() -> approved);

        var confirmation = bookingActivity.confirmBooking(request);
        LOGGER.info("Auto-upgrade booking {} completed: {}", request.bookingId(), confirmation);
        return confirmation;
    }

    @Override
    public void approve() {
        approved = true;
    }
}
