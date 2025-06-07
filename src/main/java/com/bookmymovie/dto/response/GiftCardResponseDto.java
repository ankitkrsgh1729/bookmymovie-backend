package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCardResponseDto {

    private Boolean valid;
    private BigDecimal balance;
    private BigDecimal usedAmount;
    private BigDecimal remainingBalance;
    private String message;
    private LocalDateTime expiryDate;
}