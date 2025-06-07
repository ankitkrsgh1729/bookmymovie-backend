package com.bookmymovie.dto.request;

import com.bookmymovie.entity.Show;
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
public class ShowSearchRequestDto {

    private Long movieId;
    private Long theaterId;
    private Long screenId;
    private String city;
    private LocalDate showDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Show.ShowStatus status;
    private Boolean availableOnly;
    private Integer minAvailableSeats;

    // Location-based search
    private Double latitude;
    private Double longitude;
    private Double radiusKm;

    // Filters
    private Boolean isPremiere;
    private Boolean isSpecialScreening;
    private Boolean isWeekend;
    private Boolean isPrimeTime;
    private Boolean isMatinee;
    private Boolean isLateNight;

    // Pricing filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "showDateTime";
    private String sortDirection = "ASC";
}