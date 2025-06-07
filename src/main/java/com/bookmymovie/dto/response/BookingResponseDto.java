package com.bookmymovie.dto.response;
import com.bookmymovie.constants.MovieConstant;
import com.bookmymovie.entity.Booking;
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
public class BookingResponseDto {

    private Long bookingId;
    private String bookingReference;
    private LocalDateTime bookingDate;
    private LocalDateTime expiryTime;
    private Integer numberOfSeats;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal convenienceFee;
    private BigDecimal taxes;
    private BigDecimal finalAmount;
    private Booking.BookingStatus status;
    private Booking.PaymentStatus paymentStatus;
    private Booking.PaymentMethod paymentMethod;
    private String paymentReference;
    private LocalDateTime paymentDate;
    private String contactEmail;
    private String contactPhone;
    private String specialRequests;
    private Booking.BookingSource source;
    private String cancellationReason;
    private LocalDateTime cancellationDate;
    private BigDecimal refundAmount;
    private LocalDateTime refundDate;
    private Long timeToExpiryMinutes;
    private Boolean canBeCancelled;
    private Boolean canBeModified;

    // Nested objects
    private UserDetailsDto user;
    private ShowDetailsDto show;
    private List<SeatDetailsDto> seats;

    @Data
    @Builder
    public static class UserDetailsDto {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
    }

    @Data
    @Builder
    public static class ShowDetailsDto {
        private Long showId;
        private LocalDate showDate;
        private LocalTime showTime;
        private LocalDateTime showDateTime;
        private String movieTitle;
        private String movieLanguage;
        private MovieConstant.Rating movieRating;
        private Integer movieDuration;
        private String theaterName;
        private String theaterAddress;
        private String theaterCity;
        private String screenName;
        private String screenType;
        private BigDecimal showPrice;
    }

    @Data
    @Builder
    public static class SeatDetailsDto {
        private Long seatId;
        private String rowLabel;
        private Integer seatNumber;
        private String category;
        private BigDecimal price;
        private String seatIdentifier;
    }
}