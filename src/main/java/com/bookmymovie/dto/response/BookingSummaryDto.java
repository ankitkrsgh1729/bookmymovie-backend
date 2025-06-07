package com.bookmymovie.dto.response;

import com.bookmymovie.entity.Booking;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSummaryDto {

    private Long bookingId;
    private String bookingReference;
    private LocalDateTime bookingDate;
    private String movieTitle;
    private String theaterName;
    private LocalDate showDate;
    private LocalTime showTime;
    private Integer numberOfSeats;
    private BigDecimal finalAmount;
    private Booking.BookingStatus status;
    private Booking.PaymentStatus paymentStatus;
    private Boolean canBeCancelled;
    private String seatNumbers;
}