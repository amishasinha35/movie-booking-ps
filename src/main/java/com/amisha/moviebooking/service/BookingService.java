package com.amisha.moviebooking.service;

import com.amisha.moviebooking.dto.BookingRequest;
import com.amisha.moviebooking.dto.BookingResponse;
import com.amisha.moviebooking.dto.BulkBookingRequest;
import com.amisha.moviebooking.dto.BulkBookingResponse;
import com.amisha.moviebooking.dto.BulkCancelRequest;
import com.amisha.moviebooking.dto.BulkCancelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request, Long userId, String userEmail);

    BookingResponse getBooking(Long bookingId, Long userId);

    Page<BookingResponse> getUserBookings(Long userId, Pageable pageable);

    BookingResponse cancelBooking(Long bookingId, Long userId, String reason);

    BulkBookingResponse createBookingsBulk(BulkBookingRequest request, Long userId, String userEmail);

    BulkCancelResponse cancelBookingsBulk(BulkCancelRequest request, Long userId);
}
