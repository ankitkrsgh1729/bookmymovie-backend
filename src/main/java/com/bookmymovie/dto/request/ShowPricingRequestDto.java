package com.bookmymovie.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowPricingRequestDto {

    @NotNull(message = "Show ID is required")
    private Long showId;

    private LocalDate customDate;
    private LocalTime customTime;
    private Boolean isHoliday;
}
