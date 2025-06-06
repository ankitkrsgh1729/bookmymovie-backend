package com.bookmymovie.entity;

import com.bookmymovie.constants.MovieConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_title", columnList = "title"),
        @Index(name = "idx_movie_status", columnList = "status"),
        @Index(name = "idx_movie_release_date", columnList = "releaseDate"),
        @Index(name = "idx_movie_language", columnList = "language"),
        @Index(name = "idx_movie_rating", columnList = "rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "Movie title cannot be blank")
    @Size(max = 200, message = "Movie title cannot exceed 200 characters")
    private String title;

    @Column(name = "description", length = 2000)
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    @NotNull(message = "Language is required")
    private MovieConstant.Language language;

    @ElementCollection(targetClass = MovieConstant.Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "genre")
    @NotEmpty(message = "At least one genre is required")
    private Set<MovieConstant.Genre> genres;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false)
    @NotNull(message = "Rating is required")
    private MovieConstant.Rating rating;

    @Column(name = "release_date", nullable = false)
    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Status is required")
    @Builder.Default
    private MovieConstant.MovieStatus status = MovieConstant.MovieStatus.COMING_SOON;

    @Column(name = "director", length = 100)
    @Size(max = 100, message = "Director name cannot exceed 100 characters")
    private String director;

    @ElementCollection
    @CollectionTable(
            name = "movie_cast",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "actor_name", length = 100)
    private Set<String> cast;

    @Column(name = "poster_url", length = 500)
    @Size(max = 500, message = "Poster URL cannot exceed 500 characters")
    private String posterUrl;

    @Column(name = "trailer_url", length = 500)
    @Size(max = 500, message = "Trailer URL cannot exceed 500 characters")
    private String trailerUrl;

    @Column(name = "imdb_rating", precision = 3, scale = 1)
    @DecimalMin(value = "0.0", message = "IMDB rating cannot be negative")
    @DecimalMax(value = "10.0", message = "IMDB rating cannot exceed 10.0")
    private BigDecimal imdbRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version; // For optimistic locking

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}