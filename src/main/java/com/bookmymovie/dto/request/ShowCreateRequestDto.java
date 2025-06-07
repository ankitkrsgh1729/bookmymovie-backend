package com.bookmymovie.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowCreateRequestDto {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Show date is required")
    @Future(message = "Show date must be in the future")
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    private LocalTime showTime;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
    private BigDecimal basePrice;

    @DecimalMin(value = "1.0", message = "Weekend multiplier must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Weekend multiplier cannot exceed 5.0")
    private BigDecimal weekendMultiplier;

    @DecimalMin(value = "1.0", message = "Prime time multiplier must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Prime time multiplier cannot exceed 5.0")
    private BigDecimal primeTimeMultiplier;

    @DecimalMin(value = "1.0", message = "Holiday multiplier must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Holiday multiplier cannot exceed 5.0")
    private BigDecimal holidayMultiplier;

    private Boolean hasIntermission;

    @Min(value = 5, message = "Intermission duration must be at least 5 minutes")
    @Max(value = 30, message = "Intermission duration cannot exceed 30 minutes")
    private Integer intermissionDurationMinutes;

    @Min(value = 10, message = "Cleaning time must be at least 10 minutes")
    @Max(value = 60, message = "Cleaning time cannot exceed 60 minutes")
    private Integer cleaningTimeMinutes;

    private Boolean isSpecialScreening;
    private Boolean isPremiere;
    private Boolean isHoliday;

    @Size(max = 500, message = "Special notes cannot exceed 500 characters")
    private String specialNotes;
}