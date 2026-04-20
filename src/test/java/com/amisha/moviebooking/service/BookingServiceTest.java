package com.amisha.moviebooking.service;

import com.amisha.moviebooking.discount.AfternoonShowDiscountStrategy;
import com.amisha.moviebooking.discount.DiscountEngine;
import com.amisha.moviebooking.discount.ThirdTicketDiscountStrategy;
import com.amisha.moviebooking.dto.BookingRequest;
import com.amisha.moviebooking.dto.BookingResponse;
import com.amisha.moviebooking.entity.Booking;
import com.amisha.moviebooking.entity.Seat;
import com.amisha.moviebooking.entity.Show;
import com.amisha.moviebooking.entity.Theatre;
import com.amisha.moviebooking.enums.SeatStatus;
import com.amisha.moviebooking.exception.SeatUnavailableException;
import com.amisha.moviebooking.repository.BookingRepository;
import com.amisha.moviebooking.repository.SeatRepository;
import com.amisha.moviebooking.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ShowRepository showRepository;
    @Mock
    private SeatRepository seatRepository;

    // Using real DiscountEngine with real strategies — want to verify the full calculation
    private DiscountEngine discountEngine;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        discountEngine = new DiscountEngine(List.of(
                new ThirdTicketDiscountStrategy(),
                new AfternoonShowDiscountStrategy()
        ));
        bookingService = new BookingServiceImpl(bookingRepository, showRepository, seatRepository, discountEngine);
    }

    @Test
    void createBooking_success_returnsConfirmedBooking() {
        Theatre theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("PVR");
        theatre.setCity("Mumbai");

        Show show = new Show();
        show.setId(1L);
        show.setMovieName("Dune");
        show.setTheatre(theatre);
        show.setShowTime(LocalDateTime.now().withHour(10)); // morning, no afternoon discount
        show.setTicketPrice(new BigDecimal("300.00"));
        show.setAvailableSeats(20);

        Seat seat1 = new Seat();
        seat1.setId(1L);
        seat1.setSeatNumber("A1");
        seat1.setStatus(SeatStatus.AVAILABLE);
        seat1.setShow(show);

        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setSeatNumber("A2");
        seat2.setStatus(SeatStatus.AVAILABLE);
        seat2.setShow(show);

        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(1L, 2L));

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(seatRepository.findByIdsWithLock(List.of(1L, 2L))).thenReturn(List.of(seat1, seat2));
        when(seatRepository.saveAll(anyList())).thenReturn(List.of(seat1, seat2));
        when(showRepository.save(any())).thenReturn(show);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, 1001L, "customer1@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    void createBooking_throwsWhenSeatAlreadyBooked() {
        Theatre theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("PVR");

        Show show = new Show();
        show.setId(1L);
        show.setMovieName("Dune");
        show.setTheatre(theatre);
        show.setShowTime(LocalDateTime.now().withHour(10));
        show.setTicketPrice(new BigDecimal("300.00"));
        show.setAvailableSeats(20);

        Seat bookedSeat = new Seat();
        bookedSeat.setId(1L);
        bookedSeat.setSeatNumber("A1");
        bookedSeat.setStatus(SeatStatus.BOOKED); // already taken

        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(1L));

        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(seatRepository.findByIdsWithLock(List.of(1L))).thenReturn(List.of(bookedSeat));

        assertThatThrownBy(() -> bookingService.createBooking(request, 1001L, "customer1@example.com"))
                .isInstanceOf(SeatUnavailableException.class)
                .hasMessageContaining("A1");
    }
}
