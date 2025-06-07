package com.bookmymovie.dto.request;

import com.bookmymovie.entity.Booking;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotBlank(message = "Booking reference is required")
    private String bookingReference;

    @NotNull(message = "Payment method is required")
    private Booking.PaymentMethod paymentMethod;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    // Payment details (mock for now)
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    // UPI details
    private String upiId;

    // Net banking details
    private String bankCode;

    // Wallet details
    private String walletProvider;
}