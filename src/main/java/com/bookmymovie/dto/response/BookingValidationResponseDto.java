package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingValidationResponseDto {

    private Boolean valid;
    private String message;
    private List<String> errors;
    private List<String> warnings;
    private Boolean seatsAvailable;
    private Boolean showBookable;
    private Boolean userEligible;
    private BigDecimal estimatedAmount;
}