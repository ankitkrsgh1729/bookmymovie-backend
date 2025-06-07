package com.bookmymovie.dto.response;

import com.bookmymovie.constants.MovieConstant;
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
public class BookingReceiptDto {

    private String bookingReference;
    private LocalDateTime bookingDate;
    private LocalDateTime paymentDate;
    private String customerName;
    private String contactEmail;
    private String contactPhone;

    // Show details
    private String movieTitle;
    private String movieLanguage;
    private MovieConstant.Rating movieRating;
    private Integer movieDuration;
    private String theaterName;
    private String theaterAddress;
    private String screenName;
    private LocalDate showDate;
    private LocalTime showTime;

    // Booking details
    private Integer numberOfSeats;
    private List<String> seatNumbers;
    private BigDecimal ticketPrice;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal convenienceFee;
    private BigDecimal taxes;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private String paymentReference;

    // Additional info
    private String specialRequests;
    private String qrCode; // For entry
    private String importantNotes;
}