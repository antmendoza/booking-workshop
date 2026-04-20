package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PinnedBookingWorkflow {

    String TASK_QUEUE = "BookingTaskQueue";

    @WorkflowMethod
    String processBooking(BookingRequest request);

    @SignalMethod
    void approve();
}
