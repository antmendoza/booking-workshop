package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

@WorkflowImpl(taskQueues = PinnedBookingWorkflow.TASK_QUEUE)
class PinnedBookingWorkflowImpl implements PinnedBookingWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(PinnedBookingWorkflowImpl.class);

    private final BookingActivity bookingActivity = Workflow.newActivityStub(
            BookingActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build());

    private boolean approved;

    @Override
    // TODO: Mark this workflow as PINNED using
    //       @WorkflowVersioningBehavior(VersioningBehavior.PINNED) from
    //       io.temporal.workflow.WorkflowVersioningBehavior and
    //       io.temporal.common.VersioningBehavior.
    public String processBooking(BookingRequest request) {
        LOGGER.info("Pinned booking {} waiting for approval", request.bookingId());
        Workflow.await(() -> approved);

        var confirmation = bookingActivity.confirmBooking(request);
        LOGGER.info("Pinned booking {} completed: {}", request.bookingId(), confirmation);
        return confirmation;
    }

    @Override
    public void approve() {
        approved = true;
    }
}
