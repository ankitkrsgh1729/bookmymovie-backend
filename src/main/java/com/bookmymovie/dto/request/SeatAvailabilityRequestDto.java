package com.bookmymovie.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAvailabilityRequestDto {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @Min(value = 1, message = "Required seats must be at least 1")
    @Max(value = 10, message = "Cannot check for more than 10 seats at once")
    private Integer requiredSeats;
}
