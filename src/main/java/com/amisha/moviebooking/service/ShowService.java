package com.amisha.moviebooking.service;

import com.amisha.moviebooking.dto.SeatResponse;
import com.amisha.moviebooking.dto.ShowResponse;

import java.time.LocalDate;
import java.util.List;

public interface ShowService {

    List<ShowResponse> getShowsByTheatreAndDate(Long theatreId, LocalDate date);

    List<ShowResponse> getShowsByMovieAndCity(String movieName, String city, LocalDate date);

    List<SeatResponse> getAvailableSeats(Long showId);
}
