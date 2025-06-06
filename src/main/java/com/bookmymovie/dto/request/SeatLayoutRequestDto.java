package com.bookmymovie.dto.request;

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
public class SeatLayoutRequestDto {

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotEmpty(message = "Seat configurations cannot be empty")
    private List<SeatConfigurationDto> seatConfigurations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatConfigurationDto {

        @NotBlank(message = "Row label is required")
        @Pattern(regexp = "^[A-Z]{1,2}$", message = "Row label must be 1-2 uppercase letters")
        private String rowLabel;

        @Positive(message = "Row number must be positive")
        private Integer rowNumber;

        @Positive(message = "Seats in row must be positive")
        private Integer seatsInRow;

        @NotNull(message = "Seat category is required")
        private TheaterConstant.SeatCategory category;

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
        private BigDecimal basePrice;

        private TheaterConstant.SeatType seatType;
        private Boolean isWheelchairAccessible = false;
        private Boolean isCoupleSeAT = false;
        private Boolean isPremium = false;
        private List<TheaterConstant.SeatFeature> features;
    }
}