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
 * Demonstrates that workflows with fairness weights all complete successfully.
 * Equal weights give each hotel a fair share; unequal weights bias scheduling
 * toward higher-weighted hotels under load.
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingWorkflowFairnessTest {

    @Autowired
    private WorkflowClient workflowClient;

    @Test
    void workflowsWithEqualFairnessWeights_allComplete() {
        record HotelDef(String name, String prefix) {}
        List<HotelDef> hotels = List.of(
                new HotelDef("Luxury Resort", "LR"),
                new HotelDef("City Hotel", "CH"),
                new HotelDef("Budget Inn", "BI")
        );

        List<WorkflowStub> stubs = new ArrayList<>();

        for (HotelDef hotel : hotels) {
            for (int i = 1; i <= 3; i++) {
                String bookingId = "F-%s-%03d".formatted(hotel.prefix(), i);

                var workflow = workflowClient.newWorkflowStub(
                        BookingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(BookingWorkflow.TASK_QUEUE)
                                .setWorkflowId("fairness-test-" + bookingId)
                                .setPriority(Priority.newBuilder()
                                        .setPriorityKey(3)
                                        .setFairnessKey(hotel.name())
                                        .setFairnessWeight(1.0f)
                                        .build())
                                .build());

                var request = new BookingRequest(
                        bookingId, "Guest " + i, hotel.name(), 3);
                WorkflowClient.start(workflow::processBooking, request);

                stubs.add(WorkflowStub.fromTyped(workflow));
            }
        }

        for (WorkflowStub stub : stubs) {
            String result = stub.getResult(String.class);
            assertNotNull(result,
                    "Expected a confirmation from workflow "
                            + stub.getExecution().getWorkflowId());
        }
    }

    @Test
    void workflowsWithUnequalWeights_allComplete() {
        record HotelDef(String name, String prefix, float weight) {}
        List<HotelDef> hotels = List.of(
                new HotelDef("Luxury Resort", "LR", 3.0f),
                new HotelDef("City Hotel", "CH", 1.0f),
                new HotelDef("Budget Inn", "BI", 1.0f)
        );

        List<WorkflowStub> stubs = new ArrayList<>();

        for (HotelDef hotel : hotels) {
            for (int i = 1; i <= 3; i++) {
                String bookingId = "W-%s-%03d".formatted(hotel.prefix(), i);

                var workflow = workflowClient.newWorkflowStub(
                        BookingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(BookingWorkflow.TASK_QUEUE)
                                .setWorkflowId("weighted-test-" + bookingId)
                                .setPriority(Priority.newBuilder()
                                        .setPriorityKey(3)
                                        .setFairnessKey(hotel.name())
                                        .setFairnessWeight(hotel.weight())
                                        .build())
                                .build());

                var request = new BookingRequest(
                        bookingId, "Guest " + i, hotel.name(), 3);
                WorkflowClient.start(workflow::processBooking, request);

                stubs.add(WorkflowStub.fromTyped(workflow));
            }
        }

        for (WorkflowStub stub : stubs) {
            String result = stub.getResult(String.class);
            assertNotNull(result,
                    "Expected a confirmation from workflow "
                            + stub.getExecution().getWorkflowId());
        }
    }
}
