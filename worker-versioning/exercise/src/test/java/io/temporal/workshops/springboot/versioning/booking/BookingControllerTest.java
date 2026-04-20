package io.temporal.workshops.springboot.versioning.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BookingActivityImpl mockedActivity;

    @BeforeEach
    void setUp() {
        when(mockedActivity.confirmBooking(any()))
                .thenAnswer(invocation -> {
                    BookingRequest req = invocation.getArgument(0);
                    return "Booking " + req.bookingId() + " confirmed (mocked)";
                });
    }

    @Test
    void pinnedBooking_signalApproveReturnsConfirmation() throws Exception {
        // TODO: Build a BookingRequest (id "BK-001", any name, any hotel).
        // TODO: POST it as JSON to /bookings/pinned and extract the returned
        //       workflowId from the JSON response (use ObjectMapper).
        // TODO: POST /bookings/{workflowId}/approve to unblock the workflow.
        // TODO: GET /bookings/{workflowId}/result and assert the response body
        //       contains "confirmed (mocked)".
        // TODO: verify(mockedActivity).confirmBooking(any());
    }

    @Test
    void autoUpgradeBooking_signalApproveReturnsConfirmation() throws Exception {
        // TODO: Same as above but hit /bookings/auto-upgrade.
    }
}
