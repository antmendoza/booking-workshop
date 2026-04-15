package io.temporal.workshops.springboot.priority.booking;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

@WorkflowImpl(taskQueues = BookingWorkflow.TASK_QUEUE)
class BookingWorkflowImpl implements BookingWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(BookingWorkflowImpl.class);

    private final BookingActivity bookingActivity = Workflow.newActivityStub(
            BookingActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build());

    @Override
    public String processBooking(BookingRequest request) {
        LOGGER.info("Processing booking {} for {} at {}",
                request.bookingId(), request.customerName(), request.hotelName());

        bookingActivity.validateBooking(request);
        String confirmation = bookingActivity.confirmBooking(request);

        LOGGER.info("Booking {} completed: {}", request.bookingId(), confirmation);
        return confirmation;
    }
}
