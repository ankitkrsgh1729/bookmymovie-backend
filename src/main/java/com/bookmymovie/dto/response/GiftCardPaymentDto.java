package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCardPaymentDto {

    private String giftCardNumber;
    private String pin;
    private BigDecimal amountToUse;
    private String bookingReference;
}
