package com.amisha.moviebooking.service;

import com.amisha.moviebooking.discount.BookingContext;
import com.amisha.moviebooking.discount.DiscountEngine;
import com.amisha.moviebooking.dto.BookingRequest;
import com.amisha.moviebooking.dto.BookingResponse;
import com.amisha.moviebooking.dto.BulkBookingRequest;
import com.amisha.moviebooking.dto.BulkBookingResponse;
import com.amisha.moviebooking.dto.BulkCancelRequest;
import com.amisha.moviebooking.dto.BulkCancelResponse;
import com.amisha.moviebooking.entity.Booking;
import com.amisha.moviebooking.entity.Seat;
import com.amisha.moviebooking.entity.Show;
import com.amisha.moviebooking.enums.BookingStatus;
import com.amisha.moviebooking.enums.SeatStatus;
import com.amisha.moviebooking.exception.ResourceNotFoundException;
import com.amisha.moviebooking.exception.SeatUnavailableException;
import com.amisha.moviebooking.repository.BookingRepository;
import com.amisha.moviebooking.repository.SeatRepository;
import com.amisha.moviebooking.repository.ShowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final DiscountEngine discountEngine;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ShowRepository showRepository,
                              SeatRepository seatRepository,
                              DiscountEngine discountEngine) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.discountEngine = discountEngine;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId, String userEmail) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + request.getShowId()));

        // Acquire PESSIMISTIC_WRITE lock on the requested seats to prevent double-booking
        List<Seat> seats = seatRepository.findByIdsWithLock(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more seat IDs not found");
        }

        List<Seat> unavailable = seats.stream()
                .filter(s -> s.getStatus() != SeatStatus.AVAILABLE)
                .toList();

        if (!unavailable.isEmpty()) {
            String taken = unavailable.stream()
                    .map(Seat::getSeatNumber)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("unknown");
            throw new SeatUnavailableException("Seats already taken: " + taken);
        }

        // Mark seats as booked
        seats.forEach(s -> s.setStatus(SeatStatus.BOOKED));
        seatRepository.saveAll(seats);

        // Update available seat count on show
        show.setAvailableSeats(show.getAvailableSeats() - seats.size());
        showRepository.save(show);

        // Calculate discount
        int ticketCount = seats.size();
        BigDecimal totalAmount = show.getTicketPrice().multiply(BigDecimal.valueOf(ticketCount));

        BookingContext context = new BookingContext(ticketCount, show.getTicketPrice(), show.getShowTime());
        BigDecimal discountAmount = discountEngine.calculate(context);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setUserEmail(userEmail);
        booking.setShow(show);
        booking.setSeats(seats);
        booking.setTotalAmount(totalAmount);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);

        Booking saved = bookingRepository.save(booking);

        // TODO: publish BookingConfirmedEvent to Kafka after confirming — e.g. for email notifications
        // kafkaTemplate.send("booking-confirmed", new BookingConfirmedEvent(saved.getId(), userEmail, ...));

        return BookingResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        if (!booking.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId);
        }
        return BookingResponse.from(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable)
                .map(BookingResponse::from);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        // Release seats back to available
        List<Seat> seats = booking.getSeats();
        seats.forEach(s -> s.setStatus(SeatStatus.AVAILABLE));
        seatRepository.saveAll(seats);

        // Update show available count
        Show show = booking.getShow();
        show.setAvailableSeats(show.getAvailableSeats() + seats.size());
        showRepository.save(show);

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // TODO: publish BookingCancelledEvent to Kafka — triggers refund initiation and email notification
        // kafkaTemplate.send("booking-cancelled", new BookingCancelledEvent(saved.getId(), reason));

        return BookingResponse.from(saved);
    }

    @Override
    public BulkBookingResponse createBookingsBulk(BulkBookingRequest request, Long userId, String userEmail) {
        List<BookingResponse> booked = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        for (BookingRequest br : request.getBookings()) {
            try {
                booked.add(createBooking(br, userId, userEmail));
            } catch (Exception e) {
                failures.add("showId=" + br.getShowId() + " seats=" + br.getSeatIds() + " → " + e.getMessage());
            }
        }

        return new BulkBookingResponse(booked, failures);
    }

    @Override
    @Transactional
    public BulkCancelResponse cancelBookingsBulk(BulkCancelRequest request, Long userId) {
        int cancelled = 0;
        List<Long> skippedIds = new ArrayList<>();

        for (Long bookingId : request.getBookingIds()) {
            try {
                cancelBooking(bookingId, userId, request.getReason());
                cancelled++;
            } catch (ResourceNotFoundException | IllegalStateException e) {
                // not owned by user, not found, or already cancelled — skip silently
                skippedIds.add(bookingId);
            }
        }

        return new BulkCancelResponse(cancelled, skippedIds.size(), skippedIds);
    }
}
