package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkBookingCancellationResponseDto {

    private Integer totalRequested;
    private Integer successfullyCancelled;
    private Integer failed;
    private BigDecimal totalRefundAmount;
    private List<String> successfulBookings;
    private List<BulkOperationError> errors;

    @Data
    @Builder
    public static class BulkOperationError {
        private String bookingReference;
        private String errorMessage;
        private String errorCode;
    }
}