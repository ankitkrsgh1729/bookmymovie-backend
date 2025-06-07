package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancellationRequestDto {

    @NotBlank(message = "Booking reference is required")
    private String bookingReference;

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String reason;

    private Boolean requestRefund = true;
}