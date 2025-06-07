package com.bookmymovie.dto.request;


import com.bookmymovie.entity.Booking;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequestDto {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Seat IDs cannot be empty")
    @Size(min = 1, max = 10, message = "You can book between 1 and 10 seats")
    private List<Long> seatIds;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Valid email is required")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String contactPhone;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    // Discount/Coupon information
    private String couponCode;
    private BigDecimal discountAmount;

    // Source of booking
    private Booking.BookingSource source;
}
