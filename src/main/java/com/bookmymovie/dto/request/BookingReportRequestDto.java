package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReportRequestDto {

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Long theaterId;
    private Long movieId;
    private String city;
    private String reportType; // REVENUE, OCCUPANCY, CUSTOMER_INSIGHTS, etc.
    private String groupBy; // DAILY, WEEKLY, MONTHLY, THEATER, MOVIE
    private List<String> metrics; // List of specific metrics to include
}
