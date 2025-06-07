package com.bookmymovie.dto.request;

import com.bookmymovie.entity.Booking;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingUpdateRequestDto {

    @Email(message = "Valid email is required")
    private String contactEmail;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String contactPhone;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    private Booking.BookingStatus status;
}