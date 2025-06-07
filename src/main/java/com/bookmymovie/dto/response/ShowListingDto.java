package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowListingDto {

    private Long movieId;
    private String movieTitle;
    private String movieLanguage;
    private String movieRating;
    private Integer movieDuration;
    private List<String> movieGenres;
    private String posterUrl;
    private List<TheaterShowDto> theaters;

    @Data
    @Builder
    public static class TheaterShowDto {
        private Long theaterId;
        private String theaterName;
        private String theaterAddress;
        private String city;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private List<ShowTimeDto> showTimes;
    }

    @Data
    @Builder
    public static class ShowTimeDto {
        private Long showId;
        private LocalTime showTime;
        private String screenName;
        private String screenType;
        private BigDecimal actualPrice;
        private Integer availableSeats;
        private Boolean isSpecialScreening;
        private Boolean isPremiere;
        private Boolean canBeBooked;
    }
}