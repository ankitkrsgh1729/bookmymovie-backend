package com.bookmymovie.dto.request;

import com.bookmymovie.constants.MovieConstant;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieUpdateRequestDto {

    @Size(max = 200, message = "Movie title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;

    private MovieConstant.Language language;

    @Size(max = 5, message = "Maximum 5 genres allowed")
    private Set<MovieConstant.Genre> genres;

    private MovieConstant.Rating rating;

    private LocalDate releaseDate;

    private MovieConstant.MovieStatus status;

    @Size(max = 100, message = "Director name cannot exceed 100 characters")
    private String director;

    @Size(max = 10, message = "Maximum 10 cast members allowed")
    private Set<String> cast;

    @Size(max = 500, message = "Poster URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$",
            message = "Poster URL must be a valid image URL")
    private String posterUrl;

    @Size(max = 500, message = "Trailer URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://).*", message = "Trailer URL must be a valid URL")
    private String trailerUrl;

    @DecimalMin(value = "0.0", message = "IMDB rating cannot be negative")
    @DecimalMax(value = "10.0", message = "IMDB rating cannot exceed 10.0")
    @Digits(integer = 2, fraction = 1, message = "IMDB rating must have at most 1 decimal place")
    private BigDecimal imdbRating;

    // Version for optimistic locking
    @NotNull(message = "Version is required for updates")
    private Long version;
}