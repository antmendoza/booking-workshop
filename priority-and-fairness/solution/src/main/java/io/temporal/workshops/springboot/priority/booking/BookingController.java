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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
class BookingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);

    private record CompletedBooking(String workflowId, BookingRequest request) {}

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

        List<WorkflowStub> stubs = new ArrayList<>();
        for (BookingRequest request : requests) {
            stubs.add(submitBooking(request, "booking-", 1.0f));
        }

        LOGGER.info("All {} workflows submitted. Streaming completions...", stubs.size());

        // Write submission order immediately
        writeLine(out, response, "=== SUBMISSION ORDER (random) ===");
        writeLine(out, response, "");
        for (int i = 0; i < requests.size(); i++) {
            var r = requests.get(i);
            writeLine(out, response, "  %2d. [priority=%d] booking-%s  (%s)".formatted(
                    i + 1, r.priority(), r.bookingId(), r.hotelName()));
        }
        writeLine(out, response, "");
        writeLine(out, response, "=== COMPLETION ORDER (streaming) ===");
        writeLine(out, response, "");

        // Use a blocking queue to collect completions as they happen
        BlockingQueue<CompletedBooking> completions = new LinkedBlockingQueue<>();

        for (int i = 0; i < stubs.size(); i++) {
            WorkflowStub stub = stubs.get(i);
            BookingRequest request = requests.get(i);
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

    @PostMapping("/bookings/start-fairness")
    void startFairness(HttpServletResponse response)
            throws IOException, InterruptedException {
        response.setContentType("text/plain;charset=UTF-8");

        var out = response.getOutputStream();

        // Build all booking requests — all at priority 3
        List<BookingRequest> requests = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            requests.add(new BookingRequest(
                    "LR-%03d".formatted(i), "VIP Guest " + i, "Luxury Resort", 3));
            requests.add(new BookingRequest(
                    "CH-%03d".formatted(i), "Guest " + i, "City Hotel", 3));
            requests.add(new BookingRequest(
                    "BI-%03d".formatted(i), "Traveler " + i, "Budget Inn", 3));
        }

        Collections.shuffle(requests);

        List<WorkflowStub> stubs = new ArrayList<>();
        for (BookingRequest request : requests) {
            stubs.add(submitBooking(request, "fair-", 1.0f));
        }

        LOGGER.info("All {} workflows submitted. Streaming completions...",
                stubs.size());

        writeLine(out, response,
                "=== FAIRNESS DEMO — equal weights, same priority ===");
        writeLine(out, response, "");
        for (int i = 0; i < requests.size(); i++) {
            var r = requests.get(i);
            writeLine(out, response,
                    "  %2d. [fairness=%-14s] fair-%s".formatted(
                            i + 1, r.hotelName(), r.bookingId()));
        }
        writeLine(out, response, "");
        writeLine(out, response,
                "=== COMPLETION ORDER (expect interleaving) ===");
        writeLine(out, response, "");

        BlockingQueue<CompletedBooking> completions =
                new LinkedBlockingQueue<>();

        for (int i = 0; i < stubs.size(); i++) {
            WorkflowStub stub = stubs.get(i);
            BookingRequest request = requests.get(i);
            Thread.startVirtualThread(() -> {
                stub.getResult(String.class);
                completions.add(new CompletedBooking(
                        stub.getExecution().getWorkflowId(), request));
            });
        }

        for (int i = 0; i < stubs.size(); i++) {
            var c = completions.take();
            writeLine(out, response,
                    "  %2d. [fairness=%-14s] %-16s".formatted(
                            i + 1, c.request().hotelName(),
                            c.workflowId()));
        }

        writeLine(out, response, "");
        writeLine(out, response,
                "%d workflows completed.".formatted(stubs.size()));
    }

    @PostMapping("/bookings/start-weighted")
    void startWeighted(HttpServletResponse response)
            throws IOException, InterruptedException {
        response.setContentType("text/plain;charset=UTF-8");

        var out = response.getOutputStream();

        Map<String, Float> weightByHotel = Map.of(
                "Luxury Resort", 3.0f,
                "City Hotel", 1.0f,
                "Budget Inn", 1.0f);

        // Build all booking requests — all at priority 3
        List<BookingRequest> requests = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            requests.add(new BookingRequest(
                    "LR-%03d".formatted(i), "VIP Guest " + i, "Luxury Resort", 3));
            requests.add(new BookingRequest(
                    "CH-%03d".formatted(i), "Guest " + i, "City Hotel", 3));
            requests.add(new BookingRequest(
                    "BI-%03d".formatted(i), "Traveler " + i, "Budget Inn", 3));
        }

        Collections.shuffle(requests);

        List<WorkflowStub> stubs = new ArrayList<>();
        for (BookingRequest request : requests) {
            float weight = weightByHotel.get(request.hotelName());
            stubs.add(submitBooking(request, "weighted-", weight));
        }

        LOGGER.info("All {} workflows submitted. Streaming completions...",
                stubs.size());

        writeLine(out, response,
                "=== WEIGHTED FAIRNESS — Luxury Resort 3x weight ===");
        writeLine(out, response, "");
        for (int i = 0; i < requests.size(); i++) {
            var r = requests.get(i);
            float w = weightByHotel.get(r.hotelName());
            writeLine(out, response,
                    "  %2d. [fairness=%-14s, weight=%.1f] weighted-%s"
                            .formatted(i + 1, r.hotelName(), w,
                                    r.bookingId()));
        }
        writeLine(out, response, "");
        writeLine(out, response,
                "=== COMPLETION ORDER (expect Luxury Resort ~3x more often) ===");
        writeLine(out, response, "");

        BlockingQueue<CompletedBooking> completions =
                new LinkedBlockingQueue<>();

        for (int i = 0; i < stubs.size(); i++) {
            WorkflowStub stub = stubs.get(i);
            BookingRequest request = requests.get(i);
            Thread.startVirtualThread(() -> {
                stub.getResult(String.class);
                completions.add(new CompletedBooking(
                        stub.getExecution().getWorkflowId(), request));
            });
        }

        for (int i = 0; i < stubs.size(); i++) {
            var c = completions.take();
            float weightForHotel =
                    weightByHotel.get(c.request().hotelName());
            writeLine(out, response,
                    "  %2d. [fairness=%-14s, weight=%.1f] %-20s".formatted(
                            i + 1, c.request().hotelName(),
                            weightForHotel, c.workflowId()));
        }

        writeLine(out, response, "");
        writeLine(out, response,
                "%d workflows completed.".formatted(stubs.size()));
    }

    private WorkflowStub submitBooking(
            BookingRequest request, String idPrefix,
            float fairnessWeight) {
        String workflowId = idPrefix + request.bookingId();

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
                "[priority={}] Started workflow {} [fairness={}, weight={}]",
                request.priority(), workflowId, request.hotelName(),
                fairnessWeight);

        return WorkflowStub.fromTyped(workflow);
    }
}
