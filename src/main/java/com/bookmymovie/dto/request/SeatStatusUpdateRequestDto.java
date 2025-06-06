package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import com.bookmymovie.constants.TheaterConstant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatStatusUpdateRequestDto {

    @NotEmpty(message = "Seat IDs cannot be empty")
    private List<Long> seatIds;

    @NotNull(message = "Status is required")
    private TheaterConstant.SeatStatus status;

    private String reason; // For maintenance/blocking reasons
}
