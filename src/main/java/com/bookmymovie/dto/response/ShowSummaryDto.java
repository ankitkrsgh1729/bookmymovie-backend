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
public class ShowSummaryDto {

    private Long showId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDate showDate;
    private LocalTime showTime;
    private BigDecimal actualPrice;
    private Integer availableSeats;
    private Double occupancyPercentage;
    private Show.ShowStatus status;
    private Boolean canBeBooked;
    private Boolean isSpecialScreening;
    private Boolean isPremiere;
}