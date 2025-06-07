package com.bookmymovie.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowReportRequestDto {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Long theaterId;
    private Long movieId;
    private String city;
    private String reportType; // REVENUE, OCCUPANCY, POPULAR_SHOWS, etc.
    private String groupBy; // DAILY, WEEKLY, MONTHLY, THEATER, MOVIE
}