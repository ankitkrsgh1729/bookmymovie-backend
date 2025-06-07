package com.bookmymovie.dto.response;

import lombok.*;

import java.math.BigDecimal;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRewardsDto {

    private String bookingReference;
    private Integer pointsEarned;
    private Integer totalPoints;
    private String loyaltyTier;
    private List<String> availableRewards;
    private BigDecimal cashbackAmount;
    private String nextTierRequirement;
}
