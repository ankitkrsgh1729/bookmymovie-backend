package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAvailabilityResponseDto {

    private Long showId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime showDateTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Boolean canAccommodateRequest;
    private String availabilityStatus;
    private Integer bookedSeats;
    private List<SeatMapDto> seatMap;
    private BigDecimal basePrice;
    private LocalDateTime lastUpdated;

    @Data
    @Builder
    public static class SeatMapDto {
        private Long seatId;
        private String rowLabel;
        private Integer seatNumber;
        private String category;
        private BigDecimal price;
        private Boolean available;
        private Boolean blocked; // Temporarily held during booking process
        private String seatType;
        private List<String> features;
    }
}