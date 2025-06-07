package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingValidationRequestDto {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "Seat IDs cannot be empty")
    private List<Long> seatIds;

    private Long userId;
}