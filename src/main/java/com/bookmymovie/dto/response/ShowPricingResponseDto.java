package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowPricingResponseDto {

    private Long showId;
    private BigDecimal basePrice;
    private BigDecimal actualPrice;
    private List<PricingFactorDto> pricingFactors;
    private BigDecimal totalDiscount;
    private BigDecimal finalPrice;

    @Data
    @Builder
    public static class PricingFactorDto {
        private String factorName;
        private BigDecimal multiplier;
        private BigDecimal adjustment;
        private String description;
        private Boolean applied;
    }
}