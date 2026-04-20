package com.amisha.moviebooking.service;

import com.amisha.moviebooking.dto.PaymentRequest;
import com.amisha.moviebooking.dto.PaymentResponse;
import com.amisha.moviebooking.entity.Booking;
import com.amisha.moviebooking.entity.Payment;
import com.amisha.moviebooking.enums.BookingStatus;
import com.amisha.moviebooking.enums.PaymentStatus;
import com.amisha.moviebooking.exception.ResourceNotFoundException;
import com.amisha.moviebooking.repository.BookingRepository;
import com.amisha.moviebooking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, Long userId) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        if (!booking.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found: " + request.getBookingId());
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled booking");
        }

        // Idempotency — return existing payment if already processed
        paymentRepository.findByBookingId(booking.getId()).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.SUCCESS) {
                throw new IllegalStateException("Booking already paid. TransactionId: " + existing.getTransactionId());
            }
        });

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getFinalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // TODO: integrate with real payment gateway (Razorpay / Stripe / PayU)
        // For now: simulate 90% success rate
        boolean success = Math.random() < 0.9;

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(LocalDateTime.now());
            // TODO: publish PaymentSuccessEvent to Kafka — triggers booking confirmation email
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by bank");
            payment.setUpdatedAt(LocalDateTime.now());
        }

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId);
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for booking: " + bookingId));

        return PaymentResponse.from(payment);
    }
}
