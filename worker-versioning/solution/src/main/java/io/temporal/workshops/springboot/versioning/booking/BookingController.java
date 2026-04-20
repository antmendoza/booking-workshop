package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.client.WorkflowClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class BookingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final WorkflowClient workflowClient;

    BookingController(BookingService bookingService, WorkflowClient workflowClient) {
        this.bookingService = bookingService;
        this.workflowClient = workflowClient;
    }

    @PostMapping("/bookings/pinned")
    Map<String, String> startPinned(@RequestBody BookingRequest request) {
        LOGGER.info("POST /bookings/pinned {}", request.bookingId());
        var stub = bookingService.submitPinned(request);
        return Map.of(
                "workflowId", stub.getExecution().getWorkflowId(),
                "type", "pinned");
    }

    @PostMapping("/bookings/auto-upgrade")
    Map<String, String> startAutoUpgrade(@RequestBody BookingRequest request) {
        LOGGER.info("POST /bookings/auto-upgrade {}", request.bookingId());
        var stub = bookingService.submitAutoUpgrade(request);
        return Map.of(
                "workflowId", stub.getExecution().getWorkflowId(),
                "type", "auto-upgrade");
    }

    @PostMapping("/bookings/{workflowId}/approve")
    Map<String, String> approve(@PathVariable String workflowId) {
        LOGGER.info("POST /bookings/{}/approve", workflowId);
        bookingService.approve(workflowId);
        return Map.of(
                "status", "approved",
                "workflowId", workflowId);
    }

    @GetMapping("/bookings/{workflowId}/result")
    Map<String, String> result(@PathVariable String workflowId) {
        LOGGER.info("GET /bookings/{}/result", workflowId);
        var result = workflowClient.newUntypedWorkflowStub(workflowId)
                .getResult(String.class);
        return Map.of(
                "workflowId", workflowId,
                "result", result);
    }
}
