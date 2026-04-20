package com.amisha.moviebooking.service;

import com.amisha.moviebooking.dto.SeatResponse;
import com.amisha.moviebooking.dto.ShowResponse;
import com.amisha.moviebooking.enums.SeatStatus;
import com.amisha.moviebooking.exception.ResourceNotFoundException;
import com.amisha.moviebooking.repository.SeatRepository;
import com.amisha.moviebooking.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public ShowServiceImpl(ShowRepository showRepository, SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByTheatreAndDate(Long theatreId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return showRepository.findByTheatreIdAndShowTimeBetween(theatreId, start, end)
                .stream()
                .map(ShowResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowResponse> getShowsByMovieAndCity(String movieName, String city, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return showRepository.findByMovieNameAndCityAndDate(movieName, city, start, end)
                .stream()
                .map(ShowResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeats(Long showId) {
        showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));
        return seatRepository.findByShowIdAndStatus(showId, SeatStatus.AVAILABLE)
                .stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());
    }
}
