package com.amisha.moviebooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BulkBookingRequest {

    @NotEmpty
    @Valid
    private List<BookingRequest> bookings;

    public List<BookingRequest> getBookings() { return bookings; }
    public void setBookings(List<BookingRequest> bookings) { this.bookings = bookings; }
}
