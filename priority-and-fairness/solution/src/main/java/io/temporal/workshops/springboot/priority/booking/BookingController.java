package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.common.Priority;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final WorkflowClient workflowClient;

    BookingController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/bookings/start")
    void startBookings(HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType("text/plain;charset=UTF-8");

        var out = response.getOutputStream();

        // Build all booking requests across three tiers
        List<BookingRequest> requests = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            requests.add(new BookingRequest("LR-%03d".formatted(i), "VIP Guest " + i, "Luxury Resort", 1));
            requests.add(new BookingRequest("CH-%03d".formatted(i), "Guest " + i, "City Hotel", 3));
            requests.add(new BookingRequest("BI-%03d".formatted(i), "Traveler " + i, "Budget Inn", 5));
        }

        // Shuffle to submit in random order
        Collections.shuffle(requests);

        List<BookingRequest> submissionOrder = new ArrayList<>();
        List<WorkflowStub> stubs = new ArrayList<>();

        for (BookingRequest request : requests) {
            stubs.add(submitBooking(request));
            submissionOrder.add(request);
        }

        log.info("All {} workflows submitted. Streaming completions...", stubs.size());

        // Write submission order immediately
        writeLine(out, response, "=== SUBMISSION ORDER (random) ===");
        writeLine(out, response, "");
        for (int i = 0; i < submissionOrder.size(); i++) {
            BookingRequest r = submissionOrder.get(i);
            writeLine(out, response, "  %2d. [priority=%d] booking-%s  (%s)".formatted(
                    i + 1, r.priority(), r.bookingId(), r.hotelName()));
        }
        writeLine(out, response, "");
        writeLine(out, response, "=== COMPLETION ORDER (streaming) ===");
        writeLine(out, response, "");

        // Use a blocking queue to collect completions as they happen
        record CompletedBooking(String workflowId, BookingRequest request) {}
        BlockingQueue<CompletedBooking> completions = new LinkedBlockingQueue<>();

        for (int i = 0; i < stubs.size(); i++) {
            WorkflowStub stub = stubs.get(i);
            BookingRequest request = submissionOrder.get(i);
            Thread.startVirtualThread(() -> {
                stub.getResult(String.class);
                completions.add(new CompletedBooking(
                        stub.getExecution().getWorkflowId(), request));
            });
        }

        // Stream each completion as it arrives
        for (int i = 0; i < stubs.size(); i++) {
            CompletedBooking c = completions.take();
            writeLine(out, response, "  %2d. [priority=%d] %-18s (%s)".formatted(
                    i + 1, c.request().priority(),
                    c.workflowId(), c.request().hotelName()));
        }

        writeLine(out, response, "");
        writeLine(out, response, "%d workflows completed.".formatted(stubs.size()));
    }

    private static void writeLine(OutputStream out, HttpServletResponse response, String line)
            throws IOException {
        out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
        response.flushBuffer();
    }

    private WorkflowStub submitBooking(BookingRequest request) {
        String workflowId = "booking-" + request.bookingId();

        var workflow = workflowClient.newWorkflowStub(
                BookingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(BookingWorkflow.TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .setPriority(Priority.newBuilder()
                                .setPriorityKey(request.priority())
                                .setFairnessKey(request.hotelName())
                                .setFairnessWeight(1.0f)
                                .build())
                        .build());

        WorkflowClient.start(workflow::processBooking, request);
        log.info("[priority={}] Started workflow {} [fairness={}]",
                request.priority(), workflowId, request.hotelName());

        return WorkflowStub.fromTyped(workflow);
    }
}
