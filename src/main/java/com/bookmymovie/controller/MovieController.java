package com.bookmymovie.controller;

import com.bookmymovie.constants.MovieConstant;
import com.bookmymovie.dto.request.MovieCreateRequestDto;
import com.bookmymovie.dto.request.MovieSearchRequestDto;
import com.bookmymovie.dto.request.MovieUpdateRequestDto;
import com.bookmymovie.dto.response.ApiResponse;
import com.bookmymovie.dto.response.MovieResponseDto;
import com.bookmymovie.service.MovieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/movies")
@Validated
@Slf4j
public class MovieController {

    @Autowired
    private MovieService movieService;

    // @Ankit, how is this route allowing any user to create a movie?
    // How is genres getting saved and used.
    /**
     * Create a new movie (Admin only)
     * Handles concurrent creation attempts
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieResponseDto>> createMovie(
            @Valid @RequestBody MovieCreateRequestDto request) {

        log.info("Creating movie with title: {}", request.getTitle());

        MovieResponseDto movie = movieService.createMovie(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie created successfully", movie));
    }

    /**
     * Update existing movie (Admin only)
     * Handles optimistic locking and concurrent updates
     */
    @PutMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieResponseDto>> updateMovie(
            @PathVariable Long movieId,
            @Valid @RequestBody MovieUpdateRequestDto request) {

        log.info("Updating movie with ID: {}", movieId);

        MovieResponseDto movie = movieService.updateMovie(movieId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Movie updated successfully", movie));
    }

    /**
     * Get movie by ID (Public access)
     * Optimized for high concurrent reads
     */
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResponse<MovieResponseDto>> getMovieById(
            @PathVariable Long movieId) {

        log.debug("Fetching movie with ID: {}", movieId);

        MovieResponseDto movie = movieService.getMovieById(movieId);

        return ResponseEntity.ok(
                ApiResponse.success("Movie retrieved successfully", movie));
    }

    /**
     * Get all movies with pagination (Public access)
     * Optimized for concurrent read operations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MovieResponseDto>>> getAllMovies(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Fetching all movies - page: {}, size: {}", page, size);

        Page<MovieResponseDto> movies = movieService.getAllMovies(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(
                ApiResponse.success("Movies retrieved successfully", movies));
    }

    /**
     * Search movies with multiple criteria (Public access)
     * Handles complex concurrent search operations
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<MovieResponseDto>>> searchMovies(
            @Valid @RequestBody MovieSearchRequestDto searchRequest) {

        log.debug("Searching movies with criteria: {}", searchRequest);

        Page<MovieResponseDto> movies = movieService.searchMovies(searchRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Movies searched successfully", movies));
    }

    /**
     * Get movies by status (Public access)
     * Commonly used for homepage - highly concurrent endpoint
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<MovieResponseDto>>> getMoviesByStatus(
            @PathVariable MovieConstant.MovieStatus status) {

        log.debug("Fetching movies with status: {}", status);

        List<MovieResponseDto> movies = movieService.getMoviesByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success("Movies retrieved successfully", movies));
    }

    /**
     * Get now showing movies (Public access)
     * High-traffic endpoint for homepage
     */
    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse<List<MovieResponseDto>>> getNowShowingMovies() {

        log.debug("Fetching now showing movies");

        List<MovieResponseDto> movies = movieService.getMoviesByStatus(
                MovieConstant.MovieStatus.NOW_SHOWING);

        return ResponseEntity.ok(
                ApiResponse.success("Now showing movies retrieved successfully", movies));
    }

    /**
     * Get coming soon movies (Public access)
     * High-traffic endpoint for homepage
     */
    @GetMapping("/coming-soon")
    public ResponseEntity<ApiResponse<List<MovieResponseDto>>> getComingSoonMovies() {

        log.debug("Fetching coming soon movies");

        List<MovieResponseDto> movies = movieService.getMoviesByStatus(
                MovieConstant.MovieStatus.COMING_SOON);

        return ResponseEntity.ok(
                ApiResponse.success("Coming soon movies retrieved successfully", movies));
    }

    /**
     * Async endpoint for bulk operations (Admin only)
     * Demonstrates async processing for concurrent operations
     */
    @PostMapping("/bulk-update-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> bulkUpdateMovieStatus(
            @RequestParam List<Long> movieIds,
            @RequestParam MovieConstant.MovieStatus newStatus) {

        log.info("Bulk updating status for {} movies to {}", movieIds.size(), newStatus);

        // Process asynchronously to handle large batches
        CompletableFuture.runAsync(() -> {
            movieIds.parallelStream().forEach(movieId -> {
                try {
                    // Create update request with current version
                    MovieResponseDto currentMovie = movieService.getMovieById(movieId);
                    MovieUpdateRequestDto updateRequest = MovieUpdateRequestDto.builder()
                            .status(newStatus)
                            .version(currentMovie.getVersion())
                            .build();

                    movieService.updateMovie(movieId, updateRequest);
                    log.debug("Updated movie {} status to {}", movieId, newStatus);

                } catch (Exception e) {
                    log.error("Failed to update movie {} status: {}", movieId, e.getMessage());
                }
            });
        });

        return ResponseEntity.accepted()
                .body(ApiResponse.success("Bulk update initiated",
                        "Status update for " + movieIds.size() + " movies has been started"));
    }

    /**
     * Delete movie (Admin only)
     * Supports both soft and hard delete
     */
    @DeleteMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "false") boolean hardDelete) {

        log.info("Deleting movie with ID: {}, hardDelete: {}", movieId, hardDelete);

        movieService.deleteMovie(movieId, hardDelete);

        String message = hardDelete
                ? "Movie permanently deleted"
                : "Movie soft deleted (status changed to CANCELLED)";

        return ResponseEntity.ok(
                ApiResponse.success(message, null));
    }

    /**
     * Get movie statistics (Admin only)
     * Example of read-heavy concurrent operations
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getMovieStatistics() {

        log.debug("Fetching movie statistics");

        // These operations can run concurrently
        CompletableFuture<Long> totalMovies = CompletableFuture.supplyAsync(() ->
                movieService.getAllMovies(0, 1, "movieId", "asc").getTotalElements());

        CompletableFuture<List<MovieResponseDto>> nowShowing = CompletableFuture.supplyAsync(() ->
                movieService.getMoviesByStatus(MovieConstant.MovieStatus.NOW_SHOWING));

        CompletableFuture<List<MovieResponseDto>> comingSoon = CompletableFuture.supplyAsync(() ->
                movieService.getMoviesByStatus(MovieConstant.MovieStatus.COMING_SOON));

        try {
            // Wait for all concurrent operations to complete
            CompletableFuture.allOf(totalMovies, nowShowing, comingSoon).join();

            Object statistics = new Object() {
                public final Long total = totalMovies.get();
                public final Integer nowShowingCount = nowShowing.get().size();
                public final Integer comingSoonCount = comingSoon.get().size();
            };

            return ResponseEntity.ok(
                    ApiResponse.success("Movie statistics retrieved successfully", statistics));

        } catch (Exception e) {
            log.error("Error fetching movie statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch movie statistics"));
        }
    }
}