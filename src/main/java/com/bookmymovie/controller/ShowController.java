package com.bookmymovie.controller;

import com.bookmymovie.dto.request.*;
import com.bookmymovie.dto.response.*;
import com.bookmymovie.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Show Management", description = "APIs for managing movie shows and schedules")
public class ShowController {

    private final ShowService showService;

    // ==================== ADMIN ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new show", description = "Create a new movie show for a specific screen and time")
    public ResponseEntity<ShowResponseDto> createShow(
            @Valid @RequestBody ShowCreateRequestDto request) {
        log.info("Creating show for movie {} at screen {}", request.getMovieId(), request.getScreenId());

        ShowResponseDto response = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create multiple shows", description = "Create shows in bulk for a movie across multiple dates and times")
    public ResponseEntity<BulkShowResponseDto> createBulkShows(
            @Valid @RequestBody BulkShowCreateRequestDto request) {
        log.info("Creating bulk shows for movie {} from {} to {}",
                request.getMovieId(), request.getStartDate(), request.getEndDate());

        BulkShowResponseDto response = showService.createBulkShows(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{showId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update show details", description = "Update show information like timing, pricing, or status")
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable Long showId,
            @Valid @RequestBody ShowUpdateRequestDto request) {
        log.info("Updating show with ID: {}", showId);

        ShowResponseDto response = showService.updateShow(showId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{showId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a show", description = "Cancel a scheduled show with a reason")
    public ResponseEntity<Void> cancelShow(
            @PathVariable Long showId,
            @RequestParam String reason) {
        log.info("Cancelling show {} with reason: {}", showId, reason);

        showService.cancelShow(showId, reason);
        return ResponseEntity.noContent().build();
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @GetMapping("/{showId}")
    @Operation(summary = "Get show details", description = "Get detailed information about a specific show")
    public ResponseEntity<ShowResponseDto> getShowById(@PathVariable Long showId) {
        ShowResponseDto response = showService.getShowById(showId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get shows by movie", description = "Get all shows for a specific movie on a given date")
    public ResponseEntity<List<ShowSummaryDto>> getShowsByMovie(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Show date in YYYY-MM-DD format") LocalDate showDate) {

        List<ShowSummaryDto> response = showService.getShowsByMovie(movieId, showDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movie/{movieId}/listing")
    @Operation(summary = "Get movie show listing", description = "Get comprehensive show listing for a movie in a specific city")
    public ResponseEntity<ShowListingDto> getMovieShowListing(
            @PathVariable Long movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate) {

        ShowListingDto response = showService.getMovieShowListing(movieId, city, showDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search shows", description = "Search shows with various filters and pagination")
    public ResponseEntity<Page<ShowResponseDto>> searchShows(
            @Parameter(description = "Movie ID filter") @RequestParam(required = false) Long movieId,
            @Parameter(description = "Theater ID filter") @RequestParam(required = false) Long theaterId,
            @Parameter(description = "Screen ID filter") @RequestParam(required = false) Long screenId,
            @Parameter(description = "City filter") @RequestParam(required = false) String city,
            @Parameter(description = "Show date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
            @Parameter(description = "Start date for range filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for range filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Show only available shows") @RequestParam(required = false, defaultValue = "true") Boolean availableOnly,
            @Parameter(description = "Minimum available seats") @RequestParam(required = false) Integer minAvailableSeats,
            @Parameter(description = "Show only premiere shows") @RequestParam(required = false) Boolean isPremiere,
            @Parameter(description = "Show only special screenings") @RequestParam(required = false) Boolean isSpecialScreening,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "showDateTime") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDirection) {

        ShowSearchRequestDto searchRequest = ShowSearchRequestDto.builder()
                .movieId(movieId)
                .theaterId(theaterId)
                .screenId(screenId)
                .city(city)
                .showDate(showDate)
                .startDate(startDate)
                .endDate(endDate)
                .availableOnly(availableOnly)
                .minAvailableSeats(minAvailableSeats)
                .isPremiere(isPremiere)
                .isSpecialScreening(isSpecialScreening)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<ShowResponseDto> response = showService.searchShows(searchRequest);
        return ResponseEntity.ok(response);
    }

    // ==================== CONFLICT & VALIDATION ENDPOINTS ====================

    @PostMapping("/check-conflicts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check show conflicts", description = "Check if a show schedule conflicts with existing shows")
    public ResponseEntity<ShowConflictCheckResponseDto> checkShowConflicts(
            @Valid @RequestBody ShowConflictCheckRequestDto request) {

        ShowConflictCheckResponseDto response = showService.checkShowConflicts(request);
        return ResponseEntity.ok(response);
    }

    // ==================== PRICING ENDPOINTS ====================

    @PostMapping("/pricing")
    @Operation(summary = "Calculate show pricing", description = "Calculate actual pricing for a show with all multipliers applied")
    public ResponseEntity<ShowPricingResponseDto> calculateShowPricing(
            @Valid @RequestBody ShowPricingRequestDto request) {

        ShowPricingResponseDto response = showService.calculateShowPricing(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{showId}/pricing")
    @Operation(summary = "Get show pricing", description = "Get current pricing information for a specific show")
    public ResponseEntity<ShowPricingResponseDto> getShowPricing(@PathVariable Long showId) {
        ShowPricingRequestDto request = ShowPricingRequestDto.builder()
                .showId(showId)
                .build();

        ShowPricingResponseDto response = showService.calculateShowPricing(request);
        return ResponseEntity.ok(response);
    }

    // ==================== SEAT MANAGEMENT ENDPOINTS ====================

    @PostMapping("/{showId}/book-seats")
    @Operation(summary = "Book seats for show", description = "Book a specific number of seats for a show")
    public ResponseEntity<Void> bookSeats(
            @PathVariable Long showId,
            @RequestParam Integer seatsToBook) {

        showService.bookSeats(showId, seatsToBook);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{showId}/release-seats")
    @Operation(summary = "Release seats for show", description = "Release previously booked seats for a show")
    public ResponseEntity<Void> releaseSeats(
            @PathVariable Long showId,
            @RequestParam Integer seatsToRelease) {

        showService.releaseSeats(showId, seatsToRelease);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/seat-availability")
    @Operation(summary = "Check seat availability", description = "Check if required number of seats are available for a show")
    public ResponseEntity<SeatAvailabilityResponseDto> checkSeatAvailability(
            @Valid @RequestBody SeatAvailabilityRequestDto request) {

        SeatAvailabilityResponseDto response = showService.checkSeatAvailability(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{showId}/availability")
    @Operation(summary = "Get show availability", description = "Get current seat availability for a specific show")
    public ResponseEntity<SeatAvailabilityResponseDto> getShowAvailability(@PathVariable Long showId) {
        SeatAvailabilityRequestDto request = SeatAvailabilityRequestDto.builder()
                .showId(showId)
                .requiredSeats(1)
                .build();

        SeatAvailabilityResponseDto response = showService.checkSeatAvailability(request);
        return ResponseEntity.ok(response);
    }

    // ==================== ANALYTICS ENDPOINTS ====================

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get show analytics", description = "Get comprehensive analytics for shows within a date range")
    public ResponseEntity<ShowAnalyticsDto> getShowAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ShowAnalyticsDto response = showService.getShowAnalytics(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==================== SPECIALIZED SEARCH ENDPOINTS ====================

    @GetMapping("/premieres")
    @Operation(summary = "Get upcoming premieres", description = "Get all upcoming premiere shows")
    public ResponseEntity<List<ShowSummaryDto>> getUpcomingPremieres() {
        // This would be implemented in the service layer
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/special-screenings")
    @Operation(summary = "Get special screenings", description = "Get all upcoming special screenings")
    public ResponseEntity<List<ShowSummaryDto>> getSpecialScreenings() {
        // This would be implemented in the service layer
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/housefull")
    @Operation(summary = "Get housefull shows", description = "Get all shows that are completely booked")
    public ResponseEntity<List<ShowSummaryDto>> getHousefullShows() {
        // This would be implemented in the service layer
        return ResponseEntity.ok().build(); // Placeholder
    }

    // ==================== THEATER SPECIFIC ENDPOINTS ====================

    @GetMapping("/theater/{theaterId}")
    @Operation(summary = "Get shows by theater", description = "Get all shows for a specific theater on a given date")
    public ResponseEntity<List<ShowSummaryDto>> getShowsByTheater(
            @PathVariable Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate) {

        // This would need to be implemented in the service layer
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/screen/{screenId}")
    @Operation(summary = "Get shows by screen", description = "Get all shows for a specific screen on a given date")
    public ResponseEntity<List<ShowSummaryDto>> getShowsByScreen(
            @PathVariable Long screenId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate) {

        // This would need to be implemented in the service layer
        return ResponseEntity.ok().build(); // Placeholder
    }

    // ==================== TIME-BASED ENDPOINTS ====================

    @GetMapping("/today")
    @Operation(summary = "Get today's shows", description = "Get all shows scheduled for today")
    public ResponseEntity<List<ShowSummaryDto>> getTodaysShows(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long movieId) {

        LocalDate today = LocalDate.now();
        if (movieId != null) {
            List<ShowSummaryDto> response = showService.getShowsByMovie(movieId, today);
            return ResponseEntity.ok(response);
        }

        // This would need additional implementation for city-based filtering
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming shows", description = "Get shows starting within the next few hours")
    public ResponseEntity<List<ShowSummaryDto>> getUpcomingShows(
            @RequestParam(defaultValue = "2") Integer withinHours) {

        // This would need to be implemented in the service layer
        return ResponseEntity.ok().build(); // Placeholder
    }

    // ==================== HEALTH CHECK ====================

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the show service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Show service is running");
    }
}