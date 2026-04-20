package com.amisha.moviebooking.controller;

import com.amisha.moviebooking.dto.SeatResponse;
import com.amisha.moviebooking.dto.ShowResponse;
import com.amisha.moviebooking.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@Tag(name = "Shows", description = "Browse shows by theatre and date")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping
    @Operation(summary = "Get shows for a theatre on a specific date")
    public ResponseEntity<List<ShowResponse>> getShows(
            @RequestParam Long theatreId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowsByTheatreAndDate(theatreId, date));
    }

    @GetMapping("/search")
    @Operation(summary = "Find all theatres showing a movie in a city on a given date")
    public ResponseEntity<List<ShowResponse>> searchShows(
            @RequestParam String movieName,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showService.getShowsByMovieAndCity(movieName, city, date));
    }

    @GetMapping("/{showId}/seats")
    @Operation(summary = "Get available seats for a show")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getAvailableSeats(showId));
    }
}
