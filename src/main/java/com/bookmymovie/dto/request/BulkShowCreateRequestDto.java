package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkShowCreateRequestDto {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotEmpty(message = "Show times cannot be empty")
    private List<LocalTime> showTimes;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
    private BigDecimal basePrice;

    private BigDecimal weekendMultiplier;
    private BigDecimal primeTimeMultiplier;
    private BigDecimal holidayMultiplier;
    private Boolean hasIntermission;
    private Integer intermissionDurationMinutes;
    private Integer cleaningTimeMinutes;
    private Boolean isSpecialScreening;
    private Boolean skipWeekends;
    private List<LocalDate> excludeDates;

    @Size(max = 500, message = "Special notes cannot exceed 500 characters")
    private String specialNotes;
}