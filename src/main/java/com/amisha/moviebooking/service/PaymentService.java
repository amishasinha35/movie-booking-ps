package com.amisha.moviebooking.service;

import com.amisha.moviebooking.dto.PaymentRequest;
import com.amisha.moviebooking.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request, Long userId);

    PaymentResponse getPaymentByBooking(Long bookingId, Long userId);
}
