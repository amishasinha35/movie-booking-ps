package com.amisha.moviebooking.dto;

import com.amisha.moviebooking.entity.Show;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowResponse {

    private Long id;
    private String movieName;
    private Long theatreId;
    private String theatreName;
    private String city;
    private LocalDateTime showTime;
    private BigDecimal ticketPrice;
    private int availableSeats;

    public static ShowResponse from(Show show) {
        ShowResponse dto = new ShowResponse();
        dto.id = show.getId();
        dto.movieName = show.getMovieName();
        dto.theatreId = show.getTheatre().getId();
        dto.theatreName = show.getTheatre().getName();
        dto.city = show.getTheatre().getCity();
        dto.showTime = show.getShowTime();
        dto.ticketPrice = show.getTicketPrice();
        dto.availableSeats = show.getAvailableSeats();
        return dto;
    }

    public Long getId() { return id; }
    public String getMovieName() { return movieName; }
    public Long getTheatreId() { return theatreId; }
    public String getTheatreName() { return theatreName; }
    public String getCity() { return city; }
    public LocalDateTime getShowTime() { return showTime; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public int getAvailableSeats() { return availableSeats; }
}
