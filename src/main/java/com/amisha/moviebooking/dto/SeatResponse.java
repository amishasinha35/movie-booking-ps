package com.amisha.moviebooking.dto;

import com.amisha.moviebooking.entity.Seat;
import com.amisha.moviebooking.enums.SeatStatus;

public class SeatResponse {

    private Long id;
    private String seatNumber;
    private SeatStatus status;

    public static SeatResponse from(Seat seat) {
        SeatResponse dto = new SeatResponse();
        dto.id = seat.getId();
        dto.seatNumber = seat.getSeatNumber();
        dto.status = seat.getStatus();
        return dto;
    }

    public Long getId() { return id; }
    public String getSeatNumber() { return seatNumber; }
    public SeatStatus getStatus() { return status; }
}
