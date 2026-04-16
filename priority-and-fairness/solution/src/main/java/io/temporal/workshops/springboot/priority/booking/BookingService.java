package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class BookingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BookingService.class);

    private final WorkflowClient workflowClient;

    BookingService(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    WorkflowStub submitBooking(
            BookingRequest request, String idPrefix,
            float fairnessWeight) {
        var workflowId = idPrefix + request.bookingId();

        var workflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(BookingWorkflow.TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .setPriority(Priority.newBuilder()
                                .setPriorityKey(request.priority())
                                .setFairnessKey(request.hotelName())
                                .setFairnessWeight(fairnessWeight)
                                .build())
                        .build());

        WorkflowClient.start(workflow::processBooking, request);
        LOGGER.info(
                "[priority={}] Started workflow {}"
                        + " [fairness={}, weight={}]",
                request.priority(), workflowId,
                request.hotelName(), fairnessWeight);

        return WorkflowStub.fromTyped(workflow);
    }
}
