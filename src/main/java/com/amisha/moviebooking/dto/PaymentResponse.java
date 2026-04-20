package com.amisha.moviebooking.dto;

import com.amisha.moviebooking.entity.Payment;
import com.amisha.moviebooking.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private String transactionId;
    private Long bookingId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.id = payment.getId();
        dto.transactionId = payment.getTransactionId();
        dto.bookingId = payment.getBooking().getId();
        dto.amount = payment.getAmount();
        dto.paymentMethod = payment.getPaymentMethod();
        dto.status = payment.getStatus();
        dto.failureReason = payment.getFailureReason();
        dto.createdAt = payment.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public Long getBookingId() { return bookingId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
