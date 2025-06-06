package com.bookmymovie.dto.response;

import com.bookmymovie.constants.TheaterConstant;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponseDto {

    private Long seatId;
    private String rowLabel;
    private Integer rowNumber;
    private Integer seatNumber;
    private String seatIdentifier;
    private TheaterConstant.SeatCategory category;
    private TheaterConstant.SeatStatus status;
    private BigDecimal basePrice;
    private BigDecimal effectivePrice;
    private TheaterConstant.SeatType seatType;
    private Boolean isWheelchairAccessible;
    private Boolean isCoupleSeAT;
    private Boolean isPremium;
    private List<TheaterConstant.SeatFeature> features;
}