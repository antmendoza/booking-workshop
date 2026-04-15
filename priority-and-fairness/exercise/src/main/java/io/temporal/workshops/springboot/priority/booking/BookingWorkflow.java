package io.temporal.workshops.springboot.priority.booking;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface BookingWorkflow {

    String TASK_QUEUE = "BookingTaskQueue";

    @WorkflowMethod
    String processBooking(BookingRequest request);
}
