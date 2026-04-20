package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = PinnedBookingWorkflow.TASK_QUEUE)
class BookingActivityImpl implements BookingActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingActivityImpl.class);

    private final String buildId;

    BookingActivityImpl(@Value("${app.versioning.build-id:1.0}") String buildId) {
        this.buildId = buildId;
    }

    @Override
    public String confirmBooking(BookingRequest request) {
        LOGGER.info("Confirming booking {} for {} at {} (build={})",
                request.bookingId(), request.customerName(), request.hotelName(), buildId);
        return "Booking " + request.bookingId() + " confirmed for "
                + request.customerName() + " at " + request.hotelName()
                + " — served by v" + buildId;
    }
}
