package com.bookmymovie.dto.request;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenCreateRequestDto {

    @NotBlank(message = "Screen name is required")
    @Size(min = 1, max = 50, message = "Screen name must be between 1 and 50 characters")
    private String name;

    @NotNull(message = "Theater ID is required")
    private Long theaterId;

    @NotNull(message = "Screen type is required")
    private TheaterConstant.ScreenType screenType;

    @NotNull(message = "Sound system is required")
    private TheaterConstant.SoundSystem soundSystem;

    @Positive(message = "Total rows must be positive")
    private Integer totalRows;

    @Positive(message = "Total seats must be positive")
    private Integer totalSeats;

    private List<TheaterConstant.ScreenFeature> features;
}
