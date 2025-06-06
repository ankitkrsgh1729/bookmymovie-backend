package com.bookmymovie.repository;

import com.bookmymovie.constants.MovieConstant;
import com.bookmymovie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Basic finders
    Optional<Movie> findByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCase(String title);

    // Status-based queries
    List<Movie> findByStatus(MovieConstant.MovieStatus status);
    Page<Movie> findByStatus(MovieConstant.MovieStatus status, Pageable pageable);

    // Language-based queries
    List<Movie> findByLanguage(MovieConstant.Language language);
    Page<Movie> findByLanguage(MovieConstant.Language language, Pageable pageable);

    // Genre-based queries (using contains for Set)
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g = :genre")
    List<Movie> findByGenre(@Param("genre") MovieConstant.Genre genre);

    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g = :genre")
    Page<Movie> findByGenre(@Param("genre") MovieConstant.Genre genre, Pageable pageable);

    // Date-based queries
    List<Movie> findByReleaseDateAfter(LocalDate date);
    List<Movie> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate);

    // Combined queries for common use cases
    @Query("SELECT m FROM Movie m WHERE m.status = :status AND m.language = :language")
    Page<Movie> findByStatusAndLanguage(
            @Param("status") MovieConstant.MovieStatus status,
            @Param("language") MovieConstant.Language language,
            Pageable pageable
    );

    // Search functionality (basic text search)
    @Query("SELECT m FROM Movie m WHERE " +
            "LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(m.director) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Movie> searchMovies(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Admin queries
    @Query("SELECT m FROM Movie m WHERE m.createdBy = :createdBy")
    List<Movie> findByCreatedBy(@Param("createdBy") String createdBy);

    // Count queries for analytics
    long countByStatus(MovieConstant.MovieStatus status);
    long countByLanguage(MovieConstant.Language language);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.genres g WHERE g = :genre")
    long countByGenre(@Param("genre") MovieConstant.Genre genre);

    // Recent movies
    @Query("SELECT m FROM Movie m WHERE m.status = 'NOW_SHOWING' ORDER BY m.releaseDate DESC")
    List<Movie> findRecentMovies(Pageable pageable);

    // Popular movies (we'll enhance this with ratings later)
    @Query("SELECT m FROM Movie m WHERE m.status = 'NOW_SHOWING' AND m.imdbRating IS NOT NULL ORDER BY m.imdbRating DESC")
    List<Movie> findPopularMovies(Pageable pageable);

    // Search by cast member
    @Query("SELECT m FROM Movie m JOIN m.cast c WHERE LOWER(c) LIKE LOWER(CONCAT('%', :castName, '%'))")
    Page<Movie> findByCastMemberContaining(@Param("castName") String castName, Pageable pageable);
}