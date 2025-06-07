package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPricingResponseDto {

    private Long showId;
    private Integer numberOfSeats;
    private BigDecimal baseAmount;
    private BigDecimal convenienceFee;
    private BigDecimal taxes;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private List<SeatPricingDto> seatPricing;
    private List<PricingBreakdownDto> pricingBreakdown;
    private Boolean couponApplied;
    private String couponCode;
    private BigDecimal couponDiscount;

    @Data
    @Builder
    public static class SeatPricingDto {
        private Long seatId;
        private String seatIdentifier;
        private String category;
        private BigDecimal basePrice;
        private BigDecimal finalPrice;
    }

    @Data
    @Builder
    public static class PricingBreakdownDto {
        private String component;
        private BigDecimal amount;
        private String description;
    }
}
