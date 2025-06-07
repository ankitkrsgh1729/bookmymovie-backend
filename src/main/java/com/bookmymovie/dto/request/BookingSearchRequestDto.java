package com.bookmymovie.dto.request;


import com.bookmymovie.entity.Booking;
import lombok.*;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSearchRequestDto {

    private String bookingReference;
    private String contactEmail;
    private String contactPhone;
    private Long userId;
    private Long showId;
    private Long movieId;
    private Long theaterId;
    private Booking.BookingStatus status;
    private Booking.PaymentStatus paymentStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "bookingDate";
    private String sortDirection = "DESC";
}