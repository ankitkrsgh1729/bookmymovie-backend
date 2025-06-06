package com.bookmymovie.dto.request;

import com.bookmymovie.constants.TheaterConstant;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterSearchRequestDto {

    private String city;
    private String name;

    // Geographic search
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer radiusKm = TheaterConstant.DEFAULT_SEARCH_RADIUS_KM;

    // Filters
    private TheaterConstant.TheaterType theaterType;
    private TheaterConstant.TheaterStatus status = TheaterConstant.TheaterStatus.ACTIVE;
    private List<TheaterConstant.Facility> requiredFacilities;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "name";
    private String sortDirection = "ASC";
}
