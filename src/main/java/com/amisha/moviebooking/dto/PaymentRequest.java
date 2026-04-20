package com.amisha.moviebooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull
    private Long bookingId;

    @NotBlank
    private String paymentMethod;  // UPI, CARD, NET_BANKING

    // TODO: add actual payment details (card number, UPI ID) and send to payment gateway
    // For now just accepting method — mock processes it internally

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
