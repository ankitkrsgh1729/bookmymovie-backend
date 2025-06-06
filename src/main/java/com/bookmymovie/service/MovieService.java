package com.bookmymovie.service;

import com.bookmymovie.constants.MovieConstant;
import com.bookmymovie.dto.request.MovieCreateRequestDto;
import com.bookmymovie.dto.request.MovieSearchRequestDto;
import com.bookmymovie.dto.request.MovieUpdateRequestDto;
import com.bookmymovie.dto.response.MovieResponseDto;
import com.bookmymovie.entity.Movie;
import com.bookmymovie.exception.MovieAlreadyExistsException;
import com.bookmymovie.exception.MovieNotFoundException;
import com.bookmymovie.repository.MovieRepository;
import com.bookmymovie.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    /**
     * Create a new movie
     * Handles concurrent creation attempts with same title
     */
    @Transactional
    public MovieResponseDto createMovie(MovieCreateRequestDto request) {
        log.info("Creating movie with title: {}", request.getTitle());

        // Check for duplicate title (case-insensitive)
        if (movieRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new MovieAlreadyExistsException(
                    "Movie with title '" + request.getTitle() + "' already exists"
            );
        }

        // Convert DTO to Entity
        Movie movie = Movie.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .language(request.getLanguage())
                .genres(request.getGenres())
                .rating(request.getRating())
                .releaseDate(request.getReleaseDate())
                .director(request.getDirector())
                .cast(request.getCast())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .imdbRating(request.getImdbRating())
                .createdBy(SecurityUtils.getCurrentUserEmail())
                .build();

        try {
            Movie savedMovie = movieRepository.save(movie);
            log.info("Movie created successfully with ID: {}", savedMovie.getMovieId());
            return MovieResponseDto.fromEntity(savedMovie);

        } catch (Exception e) {
            // Handle race condition where another thread created same title
            if (e.getMessage().contains("duplicate") || e.getMessage().contains("unique")) {
                throw new MovieAlreadyExistsException(
                        "Movie with title '" + request.getTitle() + "' already exists"
                );
            }
            throw e;
        }
    }

    /**
     * Update existing movie with optimistic locking
     * Handles concurrent updates to same movie
     */
    @Transactional
    public MovieResponseDto updateMovie(Long movieId, MovieUpdateRequestDto request) {
        log.info("Updating movie with ID: {}", movieId);

        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        // Check version for optimistic locking
        if (!existingMovie.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Movie has been modified by another user. Please refresh and try again."
            );
        }

        // Update only non-null fields (partial update)
        if (StringUtils.hasText(request.getTitle())) {
            // Check for duplicate title if title is being changed
            if (!existingMovie.getTitle().equalsIgnoreCase(request.getTitle()) &&
                    movieRepository.existsByTitleIgnoreCase(request.getTitle())) {
                throw new MovieAlreadyExistsException(
                        "Movie with title '" + request.getTitle() + "' already exists"
                );
            }
            existingMovie.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            existingMovie.setDescription(request.getDescription());
        }

        if (request.getDurationMinutes() != null) {
            existingMovie.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getLanguage() != null) {
            existingMovie.setLanguage(request.getLanguage());
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            existingMovie.setGenres(request.getGenres());
        }

        if (request.getRating() != null) {
            existingMovie.setRating(request.getRating());
        }

        if (request.getReleaseDate() != null) {
            existingMovie.setReleaseDate(request.getReleaseDate());
        }

        if (request.getStatus() != null) {
            existingMovie.setStatus(request.getStatus());
        }

        if (request.getDirector() != null) {
            existingMovie.setDirector(request.getDirector());
        }

        if (request.getCast() != null) {
            existingMovie.setCast(request.getCast());
        }

        if (StringUtils.hasText(request.getPosterUrl())) {
            existingMovie.setPosterUrl(request.getPosterUrl());
        }

        if (StringUtils.hasText(request.getTrailerUrl())) {
            existingMovie.setTrailerUrl(request.getTrailerUrl());
        }

        if (request.getImdbRating() != null) {
            existingMovie.setImdbRating(request.getImdbRating());
        }

        existingMovie.setUpdatedBy(SecurityUtils.getCurrentUserEmail());
        existingMovie.setUpdatedAt(LocalDateTime.now());

        try {
            Movie updatedMovie = movieRepository.save(existingMovie);
            log.info("Movie updated successfully with ID: {}", updatedMovie.getMovieId());
            return MovieResponseDto.fromEntity(updatedMovie);

        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException(
                    "Movie has been modified by another user. Please refresh and try again."
            );
        }
    }

    /**
     * Get movie by ID
     * Read operation - no transaction needed
     */
    @Transactional(readOnly = true)
    public MovieResponseDto getMovieById(Long movieId) {
        log.debug("Fetching movie with ID: {}", movieId);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        return MovieResponseDto.fromEntity(movie);
    }

    /**
     * Delete movie (soft delete by changing status)
     * Hard delete for admin operations
     */
    @Transactional
    public void deleteMovie(Long movieId, boolean hardDelete) {
        log.info("Deleting movie with ID: {}, hardDelete: {}", movieId, hardDelete);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        if (hardDelete) {
            movieRepository.delete(movie);
            log.info("Movie hard deleted with ID: {}", movieId);
        } else {
            // Soft delete - change status to CANCELLED
            movie.setStatus(MovieConstant.MovieStatus.CANCELLED);
            movie.setUpdatedBy(SecurityUtils.getCurrentUserEmail());
            movie.setUpdatedAt(LocalDateTime.now());
            movieRepository.save(movie);
            log.info("Movie soft deleted (status changed to CANCELLED) with ID: {}", movieId);
        }
    }

    /**
     * Search movies with multiple criteria
     * Optimized for concurrent read operations
     */
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> searchMovies(MovieSearchRequestDto searchRequest) {
        log.debug("Searching movies with criteria: {}", searchRequest);

        // Create pageable with sorting
        Sort sort = createSort(searchRequest.getSortBy(), searchRequest.getSortDirection());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Movie> moviePage;

        // Basic search implementation
        // TODO: Replace with Elasticsearch for complex searches
        if (StringUtils.hasText(searchRequest.getSearchTerm())) {
            moviePage = movieRepository.searchMovies(searchRequest.getSearchTerm(), pageable);
        } else if (StringUtils.hasText(searchRequest.getCastMember())) {
            moviePage = movieRepository.findByCastMemberContaining(searchRequest.getCastMember(), pageable);
        } else if (searchRequest.getLanguage() != null && searchRequest.getStatus() != null) {
            moviePage = movieRepository.findByStatusAndLanguage(
                    searchRequest.getStatus(), searchRequest.getLanguage(), pageable);
        } else if (searchRequest.getStatus() != null) {
            moviePage = movieRepository.findByStatus(searchRequest.getStatus(), pageable);
        } else if (searchRequest.getLanguage() != null) {
            moviePage = movieRepository.findByLanguage(searchRequest.getLanguage(), pageable);
        } else {
            moviePage = movieRepository.findAll(pageable);
        }

        // Convert to DTOs
        return moviePage.map(MovieResponseDto::fromEntity);
    }

    /**
     * Get all movies (with pagination)
     * Optimized for high concurrent reads
     */
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> getAllMovies(int page, int size, String sortBy, String sortDirection) {
        log.debug("Fetching all movies - page: {}, size: {}", page, size);

        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Movie> moviePage = movieRepository.findAll(pageable);
        return moviePage.map(MovieResponseDto::fromEntity);
    }

    /**
     * Get movies by status (commonly used for homepage)
     */
    @Transactional(readOnly = true)
    public List<MovieResponseDto> getMoviesByStatus(MovieConstant.MovieStatus status) {
        log.debug("Fetching movies with status: {}", status);

        List<Movie> movies = movieRepository.findByStatus(status);
        return movies.stream()
                .map(MovieResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to create Sort object
     */
    private Sort createSort(String sortBy, String sortDirection) {
        // Default sorting
        if (!StringUtils.hasText(sortBy)) {
            sortBy = "createdAt";
        }

        // Validate sort direction
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
}
