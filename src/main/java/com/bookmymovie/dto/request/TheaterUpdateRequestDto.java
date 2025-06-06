package com.bookmymovie.dto.request;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterUpdateRequestDto {

    @Size(min = 2, max = 100, message = "Theater name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private TheaterConstant.TheaterStatus status;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private List<TheaterConstant.Facility> facilities;
}