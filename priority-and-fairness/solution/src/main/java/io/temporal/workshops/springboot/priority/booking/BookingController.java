package io.temporal.workshops.springboot.priority.booking;

import io.temporal.client.WorkflowStub;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@RestController
class BookingController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BookingController.class);

    private record Tier(
            String idPrefix, String guestPrefix,
            String hotelName) {}

    private static final List<Tier> TIERS = List.of(
            new Tier("LR", "VIP Guest", "Luxury Resort"),
            new Tier("CH", "Guest", "City Hotel"),
            new Tier("BI", "Traveler", "Budget Inn"));

    private record CompletedBooking(
            String workflowId, BookingRequest request) {}

    private final BookingService bookingService;

    BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings/start")
    void startBookings(HttpServletResponse response)
            throws IOException, InterruptedException {
        var priorities = Map.of(
                "Luxury Resort", 1,
                "City Hotel", 3,
                "Budget Inn", 5);
        runDemo(response,
                "SUBMISSION ORDER (random)",
                "COMPLETION ORDER (streaming)",
                "booking-",
                buildRequests(t -> priorities.get(t.hotelName())),
                r -> 1.0f);
    }

    @PostMapping("/bookings/start-fairness")
    void startFairness(HttpServletResponse response)
            throws IOException, InterruptedException {
        runDemo(response,
                "FAIRNESS DEMO — equal weights, same priority",
                "COMPLETION ORDER (expect interleaving)",
                "fair-",
                buildRequests(3),
                r -> 1.0f);
    }

    @PostMapping("/bookings/start-weighted")
    void startWeighted(HttpServletResponse response)
            throws IOException, InterruptedException {
        var weights = Map.of(
                "Luxury Resort", 3.0f,
                "City Hotel", 1.0f,
                "Budget Inn", 1.0f);
        runDemo(response,
                "WEIGHTED FAIRNESS — Luxury Resort 3x weight",
                "COMPLETION ORDER (expect Luxury Resort ~3x)",
                "weighted-",
                buildRequests(3),
                r -> weights.get(r.hotelName()));
    }

    private void runDemo(
            HttpServletResponse response,
            String submissionHeader,
            String completionHeader,
            String idPrefix,
            List<BookingRequest> requests,
            Function<BookingRequest, Float> weightFn)
            throws IOException, InterruptedException {
        response.setContentType("text/plain;charset=UTF-8");
        var out = response.getOutputStream();

        var stubs = new ArrayList<WorkflowStub>();
        for (var request : requests) {
            stubs.add(bookingService.submitBooking(
                    request, idPrefix,
                    weightFn.apply(request)));
        }

        LOGGER.info(
                "All {} workflows submitted."
                        + " Streaming completions...",
                stubs.size());

        writeLine(out, response,
                "=== %s ===".formatted(submissionHeader));
        writeLine(out, response, "");
        for (int i = 0; i < requests.size(); i++) {
            var r = requests.get(i);
            writeLine(out, response,
                    "  %2d. [priority=%d, hotel=%-14s] %s%s"
                            .formatted(i + 1, r.priority(),
                                    r.hotelName(), idPrefix,
                                    r.bookingId()));
        }
        writeLine(out, response, "");
        writeLine(out, response,
                "=== %s ===".formatted(completionHeader));
        writeLine(out, response, "");

        var completions =
                new LinkedBlockingQueue<CompletedBooking>();
        for (int i = 0; i < stubs.size(); i++) {
            var stub = stubs.get(i);
            var request = requests.get(i);
            stub.getResultAsync(String.class).thenAccept(r ->
                    completions.add(new CompletedBooking(
                            stub.getExecution().getWorkflowId(),
                            request)));
        }

        for (int i = 0; i < stubs.size(); i++) {
            var c = completions.take();
            writeLine(out, response,
                    "  %2d. [priority=%d, hotel=%-14s] %s"
                            .formatted(i + 1,
                                    c.request().priority(),
                                    c.request().hotelName(),
                                    c.workflowId()));
        }

        writeLine(out, response, "");
        writeLine(out, response,
                "%d workflows completed."
                        .formatted(stubs.size()));
    }

    private static List<BookingRequest> buildRequests(
            int priority) {
        return buildRequests(t -> priority);
    }

    private static List<BookingRequest> buildRequests(
            ToIntFunction<Tier> priorityFn) {
        var requests = new ArrayList<BookingRequest>();
        for (int i = 1; i <= 5; i++) {
            for (var tier : TIERS) {
                requests.add(new BookingRequest(
                        "%s-%03d".formatted(
                                tier.idPrefix(), i),
                        tier.guestPrefix() + " " + i,
                        tier.hotelName(),
                        priorityFn.applyAsInt(tier)));
            }
        }
        Collections.shuffle(requests);
        return requests;
    }

    private static void writeLine(
            OutputStream out, HttpServletResponse response,
            String line) throws IOException {
        out.write(
                (line + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
        response.flushBuffer();
    }
}
