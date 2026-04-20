package com.amisha.moviebooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateShowRequest {

    private String movieName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime showTime;

    private BigDecimal ticketPrice;

    // Allows adding more seats to a show (e.g., extra rows opened)
    private Integer additionalSeats;

    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }

    public LocalDateTime getShowTime() { return showTime; }
    public void setShowTime(LocalDateTime showTime) { this.showTime = showTime; }

    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }

    public Integer getAdditionalSeats() { return additionalSeats; }
    public void setAdditionalSeats(Integer additionalSeats) { this.additionalSeats = additionalSeats; }
}
