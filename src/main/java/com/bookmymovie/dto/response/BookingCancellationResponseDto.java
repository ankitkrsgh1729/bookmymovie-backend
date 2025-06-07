package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancellationResponseDto {

    private String bookingReference;
    private Boolean cancelled;
    private String message;
    private LocalDateTime cancellationTime;
    private BigDecimal refundAmount;
    private String refundReference;
    private LocalDateTime expectedRefundDate;
    private List<String> refundDetails;
}