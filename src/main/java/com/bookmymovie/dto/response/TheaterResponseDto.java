package com.bookmymovie.dto.response;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterResponseDto {

    private Long theaterId;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phoneNumber;
    private String email;
    private TheaterConstant.TheaterType theaterType;
    private TheaterConstant.TheaterStatus status;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private List<TheaterConstant.Facility> facilities;
    private Integer totalScreens;
    private Integer totalSeats;
    private Double distanceKm; // For geographic searches
    private List<ScreenSummaryDto> screens;

    @Data
    @Builder
    public static class ScreenSummaryDto {
        private Long screenId;
        private String name;
        private TheaterConstant.ScreenType screenType;
        private TheaterConstant.SoundSystem soundSystem;
        private Integer totalSeats;
        private TheaterConstant.ScreenStatus status;
    }
}