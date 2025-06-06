package com.bookmymovie.controller;

import com.bookmymovie.constants.TheaterConstant;
import com.bookmymovie.dto.request.*;
import com.bookmymovie.dto.response.ApiResponse;
import com.bookmymovie.dto.response.ScreenResponseDto;
import com.bookmymovie.dto.response.SeatMapResponseDto;
import com.bookmymovie.dto.response.TheaterResponseDto;
import com.bookmymovie.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/theaters")
@RequiredArgsConstructor
@Slf4j
public class TheaterController {

    private final TheaterService theaterService;

    // ==================== THEATER MANAGEMENT ====================

    /**
     * Create a new theater (Admin only)
     * Handles geographic coordinates and business validation
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TheaterResponseDto>> createTheater(
            @Valid @RequestBody TheaterCreateRequestDto request) {

        log.info("Creating theater: {}", request.getName());

        TheaterResponseDto theater = theaterService.createTheater(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Theater created successfully", theater));
    }

    /**
     * Search theaters with multiple criteria
     * Supports geographic search, city-based search, and name search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TheaterResponseDto>>> searchTheaters(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude,
            @RequestParam(defaultValue = "25") Integer radiusKm,
            @RequestParam(required = false) String theaterType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.info("Searching theaters - City: {}, Name: {}, Lat: {}, Lng: {}",
                city, name, latitude, longitude);

        // Build search request
        TheaterSearchRequestDto searchRequest = TheaterSearchRequestDto.builder()
                .city(city)
                .name(name)
                .radiusKm(radiusKm)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        // Parse geographic coordinates if provided
        if (latitude != null && longitude != null) {
            try {
                searchRequest.setLatitude(new java.math.BigDecimal(latitude));
                searchRequest.setLongitude(new java.math.BigDecimal(longitude));
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid latitude or longitude format"));
            }
        }

        if (status != null) {
            try {
                searchRequest.setStatus(
                        TheaterConstant.TheaterStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid theater status: " + status));
            }
        } else {
            // Default to ACTIVE status if not provided
            searchRequest.setStatus(TheaterConstant.TheaterStatus.ACTIVE);
        }

        // Parse theater type if provided
        if (theaterType != null) {
            try {
                searchRequest.setTheaterType(
                        TheaterConstant.TheaterType.valueOf(theaterType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid theater type: " + theaterType));
            }
        }

        Page<TheaterResponseDto> theaters = theaterService.searchTheaters(searchRequest);

        return ResponseEntity.ok(ApiResponse.success("Theaters retrieved successfully", theaters));
    }

    /**
     * Get theater by ID with complete details
     */
    @GetMapping("/{theaterId}")
    public ResponseEntity<ApiResponse<TheaterResponseDto>> getTheaterById(
            @PathVariable Long theaterId) {

        log.info("Fetching theater with ID: {}", theaterId);

        TheaterResponseDto theater = theaterService.getTheaterById(theaterId);

        return ResponseEntity.ok(ApiResponse.success("Theater retrieved successfully", theater));
    }

    /**
     * Update theater details (Admin only)
     */
    @PutMapping("/{theaterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TheaterResponseDto>> updateTheater(
            @PathVariable Long theaterId,
            @Valid @RequestBody TheaterUpdateRequestDto request) {

        log.info("Updating theater with ID: {}", theaterId);

        TheaterResponseDto theater = theaterService.updateTheater(theaterId, request);

        return ResponseEntity.ok(ApiResponse.success("Theater updated successfully", theater));
    }

    /**
     * Delete theater (Admin only)
     * Soft delete to maintain data integrity
     */
    @DeleteMapping("/{theaterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTheater(@PathVariable Long theaterId) {

        log.info("Deleting theater with ID: {}", theaterId);

        theaterService.deleteTheater(theaterId);

        return ResponseEntity.ok(ApiResponse.success("Theater deleted successfully", null));
    }

    // ==================== SCREEN MANAGEMENT ====================

    /**
     * Create a new screen in a theater (Admin only)
     */
    @PostMapping("/{theaterId}/screens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScreenResponseDto>> createScreen(
            @PathVariable Long theaterId,
            @Valid @RequestBody ScreenCreateRequestDto request) {

        log.info("Creating screen: {} for theater ID: {}", request.getName(), theaterId);

        // Set theater ID from path variable
        request.setTheaterId(theaterId);

        ScreenResponseDto screen = theaterService.createScreen(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Screen created successfully", screen));
    }

    /**
     * Get all screens for a theater
     */
    @GetMapping("/{theaterId}/screens")
    public ResponseEntity<ApiResponse<List<ScreenResponseDto>>> getScreensByTheater(
            @PathVariable Long theaterId) {

        log.info("Fetching screens for theater ID: {}", theaterId);

        List<ScreenResponseDto> screens = theaterService.getScreensByTheater(theaterId);

        return ResponseEntity.ok(ApiResponse.success("Screens retrieved successfully", screens));
    }

    /**
     * Get screen details by ID
     */
    @GetMapping("/screens/{screenId}")
    public ResponseEntity<ApiResponse<ScreenResponseDto>> getScreenById(
            @PathVariable Long screenId) {

        log.info("Fetching screen with ID: {}", screenId);

        ScreenResponseDto screen = theaterService.getScreenById(screenId);

        return ResponseEntity.ok(ApiResponse.success("Screen retrieved successfully", screen));
    }

    // ==================== SEAT LAYOUT MANAGEMENT ====================

    /**
     * Create seat layout for a screen (Admin only)
     * Bulk creation of seats based on configuration
     */
    @PostMapping("/screens/{screenId}/seats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeatMapResponseDto>> createSeatLayout(
            @PathVariable Long screenId,
            @Valid @RequestBody SeatLayoutRequestDto request) {

        log.info("Creating seat layout for screen ID: {}", screenId);

        // Set screen ID from path variable
        request.setScreenId(screenId);

        SeatMapResponseDto seatMap = theaterService.createSeatLayout(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seat layout created successfully", seatMap));
    }

    /**
     * Get seat map for a screen
     * Shows complete layout with availability
     */
    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<ApiResponse<SeatMapResponseDto>> getSeatMap(
            @PathVariable Long screenId) {

        log.info("Fetching seat map for screen ID: {}", screenId);

        SeatMapResponseDto seatMap = theaterService.getSeatMap(screenId);

        return ResponseEntity.ok(ApiResponse.success("Seat map retrieved successfully", seatMap));
    }

    /**
     * Update seat status (Admin only)
     * For maintenance, blocking, etc.
     */
    @PatchMapping("/seats/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateSeatStatus(
            @Valid @RequestBody SeatStatusUpdateRequestDto request) {

        log.info("Updating status for {} seats to: {}",
                request.getSeatIds().size(), request.getStatus());

        theaterService.updateSeatStatus(request);

        return ResponseEntity.ok(ApiResponse.success("Seat status updated successfully", null));
    }

    // ==================== SPECIALIZED ENDPOINTS ====================

    /**
     * Find theaters near a location
     * Dedicated geographic search endpoint
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<Page<TheaterResponseDto>>> findNearbyTheaters(
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam(defaultValue = "10") Integer radiusKm,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Finding theaters near lat: {}, lng: {}, radius: {}km",
                latitude, longitude, radiusKm);

        try {
            TheaterSearchRequestDto searchRequest = TheaterSearchRequestDto.builder()
                    .latitude(new java.math.BigDecimal(latitude))
                    .longitude(new java.math.BigDecimal(longitude))
                    .radiusKm(Math.min(radiusKm, 50)) // Max 50km radius
                    .page(page)
                    .size(size)
                    .sortBy("distance")
                    .sortDirection("ASC")
                    .build();

            Page<TheaterResponseDto> theaters = theaterService.searchTheaters(searchRequest);

            return ResponseEntity.ok(ApiResponse.success("Nearby theaters found", theaters));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid latitude or longitude format"));
        }
    }

    /**
     * Get theaters by city with pagination
     * Optimized for city-based browsing
     */
    @GetMapping("/by-city/{city}")
    public ResponseEntity<ApiResponse<Page<TheaterResponseDto>>> getTheatersByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "name") String sortBy) {

        log.info("Fetching theaters in city: {}", city);

        TheaterSearchRequestDto searchRequest = TheaterSearchRequestDto.builder()
                .city(city)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection("ASC")
                .build();

        Page<TheaterResponseDto> theaters = theaterService.searchTheaters(searchRequest);

        return ResponseEntity.ok(ApiResponse.success("Theaters in " + city + " retrieved successfully", theaters));
    }

    /**
     * Get available screens in a theater
     * Only returns active screens with available seats
     */
    @GetMapping("/{theaterId}/screens/available")
    public ResponseEntity<ApiResponse<List<ScreenResponseDto>>> getAvailableScreens(
            @PathVariable Long theaterId) {

        log.info("Fetching available screens for theater ID: {}", theaterId);

        List<ScreenResponseDto> screens = theaterService.getScreensByTheater(theaterId)
                .stream()
                .filter(screen -> screen.getStatus() ==
                        TheaterConstant.ScreenStatus.ACTIVE)
                .filter(screen -> screen.getAvailableSeats() > 0)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Available screens retrieved successfully", screens));
    }

    // ==================== VALIDATION ENDPOINTS ====================

    /**
     * Check if theater name is available in a city
     * Helps with form validation during theater creation
     */
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkTheaterNameAvailability(
            @RequestParam String name,
            @RequestParam String city) {

        log.info("Checking theater name availability: {} in {}", name, city);

        boolean isAvailable = !theaterService.isTheaterNameTaken(name, city);

        String message = isAvailable ?
                "Theater name is available" :
                "Theater name already exists in this city";

        return ResponseEntity.ok(ApiResponse.success(message, isAvailable));
    }

    /**
     * Health check endpoint for theater service
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Theater service is healthy", "OK"));
    }
}