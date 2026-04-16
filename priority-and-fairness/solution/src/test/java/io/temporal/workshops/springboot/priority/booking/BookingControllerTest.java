package io.temporal.workshops.springboot.priority.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingActivityImpl mockedActivity;

    @BeforeEach
    void setUp() {
        when(mockedActivity.confirmBooking(any()))
                .thenAnswer(invocation -> {
                    BookingRequest req = invocation.getArgument(0);
                    return "Booking " + req.bookingId() + " confirmed";
                });
    }

    @Test
    void startBookings_completesAllWorkflows() throws Exception {
        mockMvc.perform(post("/bookings/start"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("15 workflows completed.")));

        verify(mockedActivity, times(15)).confirmBooking(any());
    }

    @Test
    void startFairness_completesAllWorkflows() throws Exception {
        mockMvc.perform(post("/bookings/start-fairness"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("15 workflows completed.")));

        verify(mockedActivity, times(15)).confirmBooking(any());
    }

    @Test
    void startWeighted_completesAllWorkflows() throws Exception {
        mockMvc.perform(post("/bookings/start-weighted"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("15 workflows completed.")));

        verify(mockedActivity, times(15)).confirmBooking(any());
    }
}
