package com.amisha.moviebooking.dto;

import com.amisha.moviebooking.entity.Booking;
import com.amisha.moviebooking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingResponse {

    private Long id;
    private Long showId;
    private String movieName;
    private LocalDateTime showTime;
    private String theatreName;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BookingStatus status;
    private LocalDateTime bookedAt;

    public static BookingResponse from(Booking booking) {
        BookingResponse dto = new BookingResponse();
        dto.id = booking.getId();
        dto.showId = booking.getShow().getId();
        dto.movieName = booking.getShow().getMovieName();
        dto.showTime = booking.getShow().getShowTime();
        dto.theatreName = booking.getShow().getTheatre().getName();
        dto.seatNumbers = booking.getSeats().stream()
                .map(s -> s.getSeatNumber())
                .collect(Collectors.toList());
        dto.totalAmount = booking.getTotalAmount();
        dto.discountAmount = booking.getDiscountAmount();
        dto.finalAmount = booking.getFinalAmount();
        dto.status = booking.getStatus();
        dto.bookedAt = booking.getBookedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getShowId() { return showId; }
    public String getMovieName() { return movieName; }
    public LocalDateTime getShowTime() { return showTime; }
    public String getTheatreName() { return theatreName; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getBookedAt() { return bookedAt; }
}
