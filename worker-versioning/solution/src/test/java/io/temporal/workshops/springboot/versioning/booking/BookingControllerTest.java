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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        var request = new BookingRequest("BK-001", "Alice", "Luxury Resort");
        var workflowId = startAndApprove("/bookings/pinned", request);

        mockMvc.perform(get("/bookings/{workflowId}/result", workflowId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("confirmed (mocked)")));

        verify(mockedActivity).confirmBooking(any());
    }

    @Test
    void autoUpgradeBooking_signalApproveReturnsConfirmation() throws Exception {
        var request = new BookingRequest("BK-002", "Bob", "City Hotel");
        var workflowId = startAndApprove("/bookings/auto-upgrade", request);

        mockMvc.perform(get("/bookings/{workflowId}/result", workflowId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("confirmed (mocked)")));

        verify(mockedActivity).confirmBooking(any());
    }

    private String startAndApprove(String startPath, BookingRequest request) throws Exception {
        MvcResult startResult = mockMvc.perform(post(startPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").exists())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, String> body = objectMapper.readValue(
                startResult.getResponse().getContentAsString(), Map.class);
        String workflowId = body.get("workflowId");

        mockMvc.perform(post("/bookings/{workflowId}/approve", workflowId))
                .andExpect(status().isOk());

        return workflowId;
    }
}
