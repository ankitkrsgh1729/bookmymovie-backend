package com.bookmymovie.dto.request;
import com.bookmymovie.constants.MovieConstant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSearchRequestDto {

    // Search term for title, description, director
    private String searchTerm;

    // Filters
    private MovieConstant.Language language;
    private Set<MovieConstant.Genre> genres;
    private MovieConstant.Rating rating;
    private MovieConstant.MovieStatus status;

    // Cast search
    private String castMember;

    // Date range
    private LocalDate releaseDateFrom;
    private LocalDate releaseDateTo;

    // Duration range
    @Min(value = 1, message = "Minimum duration must be at least 1 minute")
    private Integer minDuration;

    @Max(value = 600, message = "Maximum duration cannot exceed 600 minutes")
    private Integer maxDuration;

    // Pagination
    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;

    // Sorting
    private String sortBy; // title, releaseDate, imdbRating, createdAt

    @Builder.Default
    private String sortDirection = "asc"; // asc or desc
}