package com.amisha.moviebooking.controller;

import com.amisha.moviebooking.config.CurrentUser;
import com.amisha.moviebooking.dto.BookingRequest;
import com.amisha.moviebooking.dto.BookingResponse;
import com.amisha.moviebooking.dto.BulkBookingRequest;
import com.amisha.moviebooking.dto.BulkBookingResponse;
import com.amisha.moviebooking.dto.BulkCancelRequest;
import com.amisha.moviebooking.dto.BulkCancelResponse;
import com.amisha.moviebooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Book and view tickets")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Book seats for a show")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        Long userId = CurrentUser.getId();
        String email = CurrentUser.getEmail();
        BookingResponse response = bookingService.createBooking(request, userId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        Long userId = CurrentUser.getId();
        return ResponseEntity.ok(bookingService.getBooking(id, userId));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Book seats across multiple shows in one request")
    public ResponseEntity<BulkBookingResponse> createBulk(@Valid @RequestBody BulkBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBookingsBulk(request, CurrentUser.getId(), CurrentUser.getEmail()));
    }

    @PostMapping("/cancel-bulk")
    @Operation(summary = "Cancel multiple bookings in one request")
    public ResponseEntity<BulkCancelResponse> cancelBulk(@Valid @RequestBody BulkCancelRequest request) {
        return ResponseEntity.ok(bookingService.cancelBookingsBulk(request, CurrentUser.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long userId = CurrentUser.getId();
        return ResponseEntity.ok(bookingService.cancelBooking(id, userId, reason));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bookings")
    // TODO: add pagination to booking history — currently defaults to page 0, size 10
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = CurrentUser.getId();
        return ResponseEntity.ok(bookingService.getUserBookings(userId, PageRequest.of(page, size)));
    }
}
