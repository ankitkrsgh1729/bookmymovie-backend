package com.bookmymovie.dto.response;

import com.bookmymovie.constants.MovieConstant;
import com.bookmymovie.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponseDto {

    private Long movieId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private MovieConstant.Language language;
    private Set<MovieConstant.Genre> genres;
    private MovieConstant.Rating rating;
    private LocalDate releaseDate;
    private MovieConstant.MovieStatus status;
    private String director;
    private Set<String> cast;
    private String posterUrl;
    private String trailerUrl;
    private BigDecimal imdbRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version; // Include version for optimistic locking on frontend
    private String createdBy;
    private String updatedBy;

    // Computed fields for convenience
    private String durationFormatted; // "2h 30m"
    private boolean isReleased; // true if release date is past
    private boolean isAvailableForBooking; // true if status is NOW_SHOWING

    public static MovieResponseDto fromEntity(Movie movie) {
        MovieResponseDto dto = MovieResponseDto.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .language(movie.getLanguage())
                .genres(movie.getGenres())
                .rating(movie.getRating())
                .releaseDate(movie.getReleaseDate())
                .status(movie.getStatus())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .imdbRating(movie.getImdbRating())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .version(movie.getVersion())
                .createdBy(movie.getCreatedBy())
                .updatedBy(movie.getUpdatedBy())
                .build();

        // Set computed fields
        dto.setDurationFormatted(formatDuration(movie.getDurationMinutes()));
        dto.setReleased(movie.getReleaseDate().isBefore(LocalDate.now()) ||
                movie.getReleaseDate().equals(LocalDate.now()));
        dto.setAvailableForBooking(movie.getStatus() == MovieConstant.MovieStatus.NOW_SHOWING);

        return dto;
    }

    private static String formatDuration(Integer minutes) {
        if (minutes == null) return null;

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "m";
        } else if (remainingMinutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + remainingMinutes + "m";
        }
    }
}