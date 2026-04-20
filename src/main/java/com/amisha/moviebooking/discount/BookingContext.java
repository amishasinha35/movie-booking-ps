package com.amisha.moviebooking.discount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingContext {

    private final int ticketCount;
    private final BigDecimal ticketPrice;
    private final LocalDateTime showTime;

    public BookingContext(int ticketCount, BigDecimal ticketPrice, LocalDateTime showTime) {
        this.ticketCount = ticketCount;
        this.ticketPrice = ticketPrice;
        this.showTime = showTime;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public LocalDateTime getShowTime() {
        return showTime;
    }
}
