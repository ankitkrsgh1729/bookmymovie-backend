package com.bookmymovie.dto.response;

import com.bookmymovie.entity.Booking;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Boolean success;
    private String message;
    private String paymentReference;
    private String bookingReference;
    private BigDecimal paidAmount;
    private Booking.PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String gatewayResponse;

    // For redirect-based payments
    private String redirectUrl;
    private String qrCodeUrl; // For UPI payments
}
