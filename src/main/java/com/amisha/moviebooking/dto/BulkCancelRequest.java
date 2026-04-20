package com.amisha.moviebooking.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BulkCancelRequest {

    @NotEmpty
    private List<Long> bookingIds;

    private String reason;

    public List<Long> getBookingIds() { return bookingIds; }
    public void setBookingIds(List<Long> bookingIds) { this.bookingIds = bookingIds; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
