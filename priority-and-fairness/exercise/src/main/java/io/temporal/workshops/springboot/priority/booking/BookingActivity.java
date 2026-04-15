package io.temporal.workshops.springboot.priority.booking;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface BookingActivity {

    @ActivityMethod
    void validateBooking(BookingRequest request);

    @ActivityMethod
    String confirmBooking(BookingRequest request);
}
