package com.amisha.moviebooking.dto;

import java.util.List;

public class BulkBookingResponse {

    private int booked;
    private int failed;
    private List<BookingResponse> bookings;
    private List<String> failures;   // reason per failed booking request

    public BulkBookingResponse(List<BookingResponse> bookings, List<String> failures) {
        this.bookings = bookings;
        this.failures = failures;
        this.booked = bookings.size();
        this.failed = failures.size();
    }

    public int getBooked() { return booked; }
    public int getFailed() { return failed; }
    public List<BookingResponse> getBookings() { return bookings; }
    public List<String> getFailures() { return failures; }
}
