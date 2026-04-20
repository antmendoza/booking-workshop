package io.temporal.workshops.springboot.versioning.booking;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface BookingActivity {

    @ActivityMethod
    String confirmBooking(BookingRequest request);
}
