package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowAnalyticsDto {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Long totalShows;
    private Long completedShows;
    private Long cancelledShows;
    private Double averageOccupancy;
    private BigDecimal totalRevenue;
    private BigDecimal averageTicketPrice;

    private List<PopularMovieDto> popularMovies;
    private List<TheaterPerformanceDto> theaterPerformance;
    private List<HourlyAnalysisDto> hourlyAnalysis;
    private List<DailyRevenueDto> dailyRevenue;
    private WeekdayWeekendAnalysisDto weekdayWeekendAnalysis;

    @Data
    @Builder
    public static class PopularMovieDto {
        private String movieTitle;
        private Long showCount;
        private Double averageOccupancy;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    public static class TheaterPerformanceDto {
        private String theaterName;
        private String city;
        private Long showCount;
        private BigDecimal totalRevenue;
        private Double averageOccupancy;
    }

    @Data
    @Builder
    public static class HourlyAnalysisDto {
        private Integer hour;
        private Long showCount;
        private Double averageOccupancy;
    }

    @Data
    @Builder
    public static class DailyRevenueDto {
        private LocalDate date;
        private BigDecimal revenue;
        private Long showCount;
    }

    @Data
    @Builder
    public static class WeekdayWeekendAnalysisDto {
        private Double weekdayOccupancy;
        private Double weekendOccupancy;
        private Long weekdayShows;
        private Long weekendShows;
    }
}