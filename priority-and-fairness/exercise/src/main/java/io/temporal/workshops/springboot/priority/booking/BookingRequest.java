package io.temporal.workshops.springboot.priority.booking;

public record BookingRequest(
        String bookingId,
        String customerName,
        String hotelName,
        int priority
) {
}
