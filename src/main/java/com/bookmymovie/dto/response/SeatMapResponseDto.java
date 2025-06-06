package com.bookmymovie.dto.response;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatMapResponseDto {

    private Long screenId;
    private String screenName;
    private Integer totalRows;
    private Integer totalSeats;
    private List<RowDto> rows;

    @Data
    @Builder
    public static class RowDto {
        private String rowLabel;
        private Integer rowNumber;
        private List<SeatDto> seats;

        @Data
        @Builder
        public static class SeatDto {
            private Long seatId;
            private Integer seatNumber;
            private String seatIdentifier;
            private TheaterConstant.SeatCategory category;
            private TheaterConstant.SeatStatus status;
            private BigDecimal price;
            private Boolean isAvailable;
            private Boolean isCoupleSeAT;
            private Boolean isPremium;
        }
    }
}