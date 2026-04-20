package com.amisha.moviebooking.controller;

import com.amisha.moviebooking.dto.CreateShowRequest;
import com.amisha.moviebooking.dto.CreateTheatreRequest;
import com.amisha.moviebooking.dto.ShowResponse;
import com.amisha.moviebooking.dto.UpdateShowRequest;
import com.amisha.moviebooking.entity.Seat;
import com.amisha.moviebooking.entity.Show;
import com.amisha.moviebooking.entity.Theatre;
import com.amisha.moviebooking.enums.SeatStatus;
import com.amisha.moviebooking.exception.ResourceNotFoundException;
import com.amisha.moviebooking.repository.SeatRepository;
import com.amisha.moviebooking.repository.ShowRepository;
import com.amisha.moviebooking.repository.TheatreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin (B2B)", description = "Theatre and show management — ADMIN role required")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final TheatreRepository theatreRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public AdminController(TheatreRepository theatreRepository,
                           ShowRepository showRepository,
                           SeatRepository seatRepository) {
        this.theatreRepository = theatreRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    @PostMapping("/theatres")
    @Operation(summary = "Register a new theatre (B2B onboarding)")
    public ResponseEntity<Theatre> createTheatre(@Valid @RequestBody CreateTheatreRequest request) {
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setCity(request.getCity());
        theatre.setAddress(request.getAddress());
        theatre.setTotalSeats(request.getTotalSeats());
        return ResponseEntity.status(HttpStatus.CREATED).body(theatreRepository.save(theatre));
    }

    @PostMapping("/shows")
    @Operation(summary = "Add a show to a theatre")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody CreateShowRequest request) {
        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + request.getTheatreId()));

        Show show = new Show();
        show.setMovieName(request.getMovieName());
        show.setTheatre(theatre);
        show.setShowTime(request.getShowTime());
        show.setTicketPrice(request.getTicketPrice());
        show.setAvailableSeats(request.getTotalSeats());
        Show saved = showRepository.save(show);

        // Auto-generate seats A1–D5 (or up to totalSeats)
        // TODO: accept explicit seat layout config for more complex theatres
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
        int remaining = request.getTotalSeats();
        outer:
        for (String row : rows) {
            for (int col = 1; col <= 10; col++) {
                if (remaining-- <= 0) break outer;
                Seat seat = new Seat();
                seat.setShow(saved);
                seat.setSeatNumber(row + col);
                seat.setStatus(SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);

        return ResponseEntity.status(HttpStatus.CREATED).body(ShowResponse.from(saved));
    }

    @PatchMapping("/shows/{showId}")
    @Operation(summary = "Update show details (time, price, or allocate additional seats)")
    public ResponseEntity<ShowResponse> updateShow(
            @PathVariable Long showId,
            @RequestBody UpdateShowRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        if (request.getMovieName() != null) show.setMovieName(request.getMovieName());
        if (request.getShowTime() != null) show.setShowTime(request.getShowTime());
        if (request.getTicketPrice() != null) show.setTicketPrice(request.getTicketPrice());

        if (request.getAdditionalSeats() != null && request.getAdditionalSeats() > 0) {
            int existingCount = seatRepository.countByShowId(showId);
            String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
            List<Seat> newSeats = new ArrayList<>();
            int added = 0;
            outer:
            for (String row : rows) {
                for (int col = 1; col <= 10; col++) {
                    // skip over seats that already exist
                    if (existingCount-- > 0) continue;
                    Seat seat = new Seat();
                    seat.setShow(show);
                    seat.setSeatNumber(row + col);
                    seat.setStatus(SeatStatus.AVAILABLE);
                    newSeats.add(seat);
                    if (++added >= request.getAdditionalSeats()) break outer;
                }
            }
            seatRepository.saveAll(newSeats);
            show.setAvailableSeats(show.getAvailableSeats() + newSeats.size());
        }

        return ResponseEntity.ok(ShowResponse.from(showRepository.save(show)));
    }

    @DeleteMapping("/shows/{showId}")
    @Operation(summary = "Cancel a show")
    public ResponseEntity<Void> cancelShow(@PathVariable Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));
        // TODO: notify affected customers via Kafka event before deleting
        showRepository.delete(show);
        return ResponseEntity.noContent().build();
    }
}
