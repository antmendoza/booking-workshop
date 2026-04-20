package io.temporal.workshops.springboot.versioning.booking;

public record BookingRequest(
        String bookingId,
        String customerName,
        String hotelName
) {
}
