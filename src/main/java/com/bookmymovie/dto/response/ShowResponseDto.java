package com.bookmymovie.dto.response;
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
public class ShowResponseDto {

    private Long showId;
    private LocalDate showDate;
    private LocalTime showTime;
    private LocalDateTime showDateTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private BigDecimal actualPrice;
    private Show.ShowStatus status;
    private Integer totalSeats;
    private Integer bookedSeats;
    private Integer availableSeats;
    private Double occupancyPercentage;
    private Boolean hasIntermission;
    private Integer intermissionDurationMinutes;
    private Boolean isSpecialScreening;
    private Boolean isPremiere;
    private Boolean isHoliday;
    private Boolean isWeekend;
    private Boolean isPrimeTime;
    private Boolean isMatinee;
    private Boolean isLateNight;
    private String specialNotes;

    // Nested objects
    private MovieDetailsDto movie;
    private TheaterDetailsDto theater;
    private ScreenDetailsDto screen;

    @Data
    @Builder
    public static class MovieDetailsDto {
        private Long movieId;
        private String title;
        private String language;
        private String rating;
        private Integer durationMinutes;
        private List<String> genres;
        private String posterUrl;
    }

    @Data
    @Builder
    public static class TheaterDetailsDto {
        private Long theaterId;
        private String name;
        private String address;
        private String city;
        private String state;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String phoneNumber;
    }

    @Data
    @Builder
    public static class ScreenDetailsDto {
        private Long screenId;
        private String name;
        private String screenType;
        private String soundSystem;
        private Integer totalSeats;
        private List<String> features;
    }
}