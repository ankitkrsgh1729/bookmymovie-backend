package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingInitiationResponseDto {

    private String bookingReference;
    private Long bookingId;
    private LocalDateTime expiryTime;
    private Long timeToExpiryMinutes;
    private BigDecimal finalAmount;
    private String message;
    private Boolean success;
    private List<String> errors;
}