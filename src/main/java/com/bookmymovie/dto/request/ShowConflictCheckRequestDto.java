package com.bookmymovie.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowConflictCheckRequestDto {

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Show date is required")
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    private LocalTime showTime;

    @NotNull(message = "Movie duration is required")
    private Integer movieDurationMinutes;

    private Boolean hasIntermission;
    private Integer intermissionDurationMinutes;
    private Integer cleaningTimeMinutes;
    private Long excludeShowId; // For updates
}