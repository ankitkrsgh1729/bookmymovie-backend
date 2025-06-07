package com.bookmymovie.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAvailabilityResponseDto {

    private Long showId;
    private Integer totalSeats;
    private Integer bookedSeats;
    private Integer availableSeats;
    private Boolean canAccommodateRequest;
    private String availabilityStatus;
    private LocalDateTime lastUpdated;
}