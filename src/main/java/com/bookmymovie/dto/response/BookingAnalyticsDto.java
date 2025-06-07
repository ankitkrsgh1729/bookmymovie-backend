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
public class BookingAnalyticsDto {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Long expiredBookings;
    private BigDecimal totalRevenue;
    private BigDecimal averageBookingValue;
    private Double averageSeatsPerBooking;
    private Double cancellationRate;

    private List<DailyStatsDto> dailyStats;
    private List<PopularMovieDto> popularMovies;
    private List<TheaterRevenueDto> theaterRevenue;
    private List<HourlyPatternDto> hourlyPattern;
    private List<PaymentMethodStatsDto> paymentMethods;
    private List<TopCustomerDto> topCustomers;

    @Data
    @Builder
    public static class DailyStatsDto {
        private LocalDate date;
        private Long bookingCount;
        private BigDecimal revenue;
        private Double averageValue;
    }

    @Data
    @Builder
    public static class PopularMovieDto {
        private String movieTitle;
        private Long bookingCount;
        private BigDecimal revenue;
        private Double averageSeats;
    }

    @Data
    @Builder
    public static class TheaterRevenueDto {
        private String theaterName;
        private Long bookingCount;
        private BigDecimal revenue;
        private Double utilization;
    }

    @Data
    @Builder
    public static class HourlyPatternDto {
        private Integer hour;
        private Long bookingCount;
        private BigDecimal averageAmount;
    }

    @Data
    @Builder
    public static class PaymentMethodStatsDto {
        private String paymentMethod;
        private Long count;
        private BigDecimal amount;
        private Double percentage;
    }

    @Data
    @Builder
    public static class TopCustomerDto {
        private String customerEmail;
        private Long bookingCount;
        private BigDecimal totalSpending;
        private String loyaltyTier;
    }
}