package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkBookingCancellationRequestDto {

    @NotEmpty(message = "Booking references cannot be empty")
    @Size(max = 50, message = "Cannot cancel more than 50 bookings at once")
    private List<String> bookingReferences;

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String reason;

    private Boolean requestRefund = true;
    private String adminNotes;
}