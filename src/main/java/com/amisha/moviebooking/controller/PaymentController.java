package com.amisha.moviebooking.controller;

import com.amisha.moviebooking.config.CurrentUser;
import com.amisha.moviebooking.dto.PaymentRequest;
import com.amisha.moviebooking.dto.PaymentResponse;
import com.amisha.moviebooking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Initiate and track payments for bookings")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Initiate payment for a booking")
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request, CurrentUser.getId()));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment status for a booking")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId, CurrentUser.getId()));
    }
}
