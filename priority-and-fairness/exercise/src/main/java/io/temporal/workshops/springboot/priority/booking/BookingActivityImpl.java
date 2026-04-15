package io.temporal.workshops.springboot.priority.booking;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = BookingWorkflow.TASK_QUEUE)
class BookingActivityImpl implements BookingActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingActivityImpl.class);

    @Override
    public void validateBooking(BookingRequest request) {
        LOGGER.info("[priority={}] Validating booking {} for {} at {}",
                request.priority(), request.bookingId(), request.customerName(), request.hotelName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String confirmBooking(BookingRequest request) {
        LOGGER.info("[priority={}] Confirming booking {} for {} at {}",
                request.priority(), request.bookingId(), request.customerName(), request.hotelName());
        return "Booking " + request.bookingId() + " confirmed for "
                + request.customerName() + " at " + request.hotelName();
    }
}
