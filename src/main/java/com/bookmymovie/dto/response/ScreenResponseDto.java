package com.bookmymovie.dto.response;

import com.bookmymovie.constants.TheaterConstant;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenResponseDto {

    private Long screenId;
    private String name;
    private Long theaterId;
    private String theaterName;
    private TheaterConstant.ScreenType screenType;
    private TheaterConstant.SoundSystem soundSystem;
    private TheaterConstant.ScreenStatus status;
    private Integer totalRows;
    private Integer totalSeats;
    private List<TheaterConstant.ScreenFeature> features;
    private Long availableSeats;
    private List<SeatResponseDto> seats;
}