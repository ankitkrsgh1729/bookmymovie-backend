package com.bookmymovie.dto.request;

import com.bookmymovie.entity.Show;
import jakarta.validation.constraints.Size;
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
public class ShowUpdateRequestDto {

    private LocalDate showDate;
    private LocalTime showTime;
    private BigDecimal basePrice;
    private BigDecimal weekendMultiplier;
    private BigDecimal primeTimeMultiplier;
    private BigDecimal holidayMultiplier;
    private Boolean hasIntermission;
    private Integer intermissionDurationMinutes;
    private Integer cleaningTimeMinutes;
    private Boolean isSpecialScreening;
    private Boolean isPremiere;
    private Boolean isHoliday;
    private Show.ShowStatus status;

    @Size(max = 500, message = "Special notes cannot exceed 500 characters")
    private String specialNotes;
}