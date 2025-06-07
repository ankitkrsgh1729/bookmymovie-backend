package com.bookmymovie.controller;

import com.bookmymovie.dto.request.*;
import com.bookmymovie.dto.response.*;
import com.bookmymovie.service.BookingService;
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
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    // ==================== BOOKING FLOW ENDPOINTS ====================

    @PostMapping
    public ResponseEntity<BookingInitiationResponseDto> createBooking(
            @Valid @RequestBody BookingCreateRequestDto request) {
        log.info("Creating booking for show {} by user {}", request.getShowId(), request.getUserId());

        BookingInitiationResponseDto response = bookingService.initiateBooking(request);

        if (response.getSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<BookingValidationResponseDto> validateBooking(
            @Valid @RequestBody BookingCreateRequestDto request) {
        log.info("Validating booking request for show {}", request.getShowId());

        BookingValidationResponseDto response = bookingService.validateBookingRequest(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pricing")
    public ResponseEntity<BookingPricingResponseDto> calculatePricing(
            @Valid @RequestBody BookingPricingRequestDto request) {
        log.info("Calculating pricing for show {} with {} seats",
                request.getShowId(), request.getSeatIds().size());

        BookingPricingResponseDto response = bookingService.calculateBookingPricing(request);
        return ResponseEntity.ok(response);
    }

    // ==================== PAYMENT ENDPOINTS ====================

    @PostMapping("/payment")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto request) {
        log.info("Processing payment for booking {}", request.getBookingReference());

        PaymentResponseDto response = bookingService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingReference}/confirm")
    public ResponseEntity<BookingConfirmationResponseDto> confirmBooking(
            @PathVariable String bookingReference) {
        log.info("Confirming booking {}", bookingReference);

        BookingConfirmationResponseDto response = bookingService.confirmBooking(bookingReference);
        return ResponseEntity.ok(response);
    }

    // ==================== BOOKING MANAGEMENT ENDPOINTS ====================

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long bookingId) {
        BookingResponseDto response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingResponseDto> getBookingByReference(@PathVariable String bookingReference) {
        BookingResponseDto response = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingService.isBookingOwner(#bookingId, authentication.name)")
    public ResponseEntity<BookingResponseDto> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingUpdateRequestDto request) {
        log.info("Updating booking {}", bookingId);

        BookingResponseDto response = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== USER BOOKING ENDPOINTS ====================

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<List<BookingSummaryDto>> getUserBookings(@PathVariable Long userId) {
        List<BookingSummaryDto> response = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/paginated")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<Page<BookingSummaryDto>> getUserBookingsPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookingSummaryDto> response = bookingService.getUserBookingsPaginated(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // ==================== CANCELLATION ENDPOINTS ====================

    @PostMapping("/cancel")
    public ResponseEntity<BookingCancellationResponseDto> cancelBooking(
            @Valid @RequestBody BookingCancellationRequestDto request) {
        log.info("Cancelling booking {}", request.getBookingReference());

        BookingCancellationResponseDto response = bookingService.cancelBooking(request);
        return ResponseEntity.ok(response);
    }

    // ==================== SEAT AVAILABILITY ENDPOINTS ====================

    @PostMapping("/seat-availability")
    public ResponseEntity<SeatAvailabilityResponseDto> getSeatAvailability(
            @Valid @RequestBody SeatAvailabilityRequestDto request) {

        SeatAvailabilityResponseDto response = bookingService.getSeatAvailability(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/show/{showId}/seats")
    public ResponseEntity<SeatAvailabilityResponseDto> getShowSeatAvailability(@PathVariable Long showId) {
        SeatAvailabilityRequestDto request = SeatAvailabilityRequestDto.builder()
                .showId(showId)
                .build();

        SeatAvailabilityResponseDto response = bookingService.getSeatAvailability(request);
        return ResponseEntity.ok(response);
    }

    // ==================== SEARCH & FILTER ENDPOINTS ====================

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BookingResponseDto>> searchBookings(
            @RequestParam(required = false) String bookingReference,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long showId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "bookingDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        // This would need implementation in the service layer
        // For now, returning a placeholder response
        return ResponseEntity.ok().build();
    }

    // ==================== RECEIPT & CONFIRMATION ENDPOINTS ====================

    @GetMapping("/{bookingReference}/receipt")
    public ResponseEntity<BookingReceiptDto> getBookingReceipt(@PathVariable String bookingReference) {
        // This would generate and return a receipt for the booking
        BookingResponseDto booking = bookingService.getBookingByReference(bookingReference);

        BookingReceiptDto bookingReceiptDto = new BookingReceiptDto();
        bookingReceiptDto.setBookingReference(booking.getBookingReference());
        bookingReceiptDto.setBookingDate(booking.getBookingDate());
        bookingReceiptDto.setPaymentDate(booking.getPaymentDate());
        bookingReceiptDto.setCustomerName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        bookingReceiptDto.setContactEmail(booking.getContactEmail());
        bookingReceiptDto.setContactPhone(booking.getContactPhone());
        bookingReceiptDto.setMovieTitle(booking.getShow().getMovieTitle());
        bookingReceiptDto.setMovieLanguage(booking.getShow().getMovieLanguage());
        bookingReceiptDto.setMovieRating(booking.getShow().getMovieRating());
        bookingReceiptDto.setMovieDuration(booking.getShow().getMovieDuration());
        bookingReceiptDto.setTheaterName(booking.getShow().getTheaterName());
        bookingReceiptDto.setTheaterAddress(booking.getShow().getTheaterAddress());
        bookingReceiptDto.setScreenName(booking.getShow().getScreenName());
        // For now, return success - actual receipt generation would be implemented
        return ResponseEntity.ok(bookingReceiptDto);
    }

    @GetMapping("/{bookingReference}/qr-code")
    public ResponseEntity<String> getBookingQrCode(@PathVariable String bookingReference) {
        // This would return the QR code for the booking
        BookingResponseDto booking = bookingService.getBookingByReference(bookingReference);

        // Mock QR code response
        String qrCode = "QR:" + bookingReference + ":" + booking.getShow().getShowId();
        return ResponseEntity.ok(qrCode);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingSummaryDto>> getTodaysBookings(
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) String status) {

        // This would return today's bookings for admin dashboard
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingAnalyticsDto> getBookingStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) Long movieId) {

        // This would return booking statistics and analytics
        return ResponseEntity.ok().build();
    }

    // ==================== QUICK ACTION ENDPOINTS ====================

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<BookingSummaryDto>> getUpcomingBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "7") int days) {

        // This would return upcoming bookings for a user or all users (admin)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<BookingSummaryDto>> getRecentBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "30") int days) {

        // This would return recent bookings
        return ResponseEntity.ok().build();
    }

    // ==================== SHOW-SPECIFIC ENDPOINTS ====================

    @GetMapping("/show/{showId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingSummaryDto>> getShowBookings(@PathVariable Long showId) {
        // This would return all bookings for a specific show
        return ResponseEntity.ok().build();
    }

    @GetMapping("/show/{showId}/occupancy")
    public ResponseEntity<Object> getShowOccupancy(@PathVariable Long showId) {
        // This would return occupancy information for a show
        SeatAvailabilityRequestDto request = SeatAvailabilityRequestDto.builder()
                .showId(showId)
                .build();

        SeatAvailabilityResponseDto availability = bookingService.getSeatAvailability(request);

        Object occupancyInfo = new Object() {
            public final Long showId = availability.getShowId();
            public final Integer totalSeats = availability.getTotalSeats();
            public final Integer bookedSeats = availability.getBookedSeats();
            public final Integer availableSeats = availability.getAvailableSeats();
            public final Double occupancyPercentage = (bookedSeats * 100.0) / totalSeats;
            public final String status = occupancyPercentage >= 90 ? "HOUSEFULL" :
                    occupancyPercentage >= 70 ? "FILLING_FAST" : "AVAILABLE";
        };

        return ResponseEntity.ok(occupancyInfo);
    }

    // ==================== THEATER MANAGEMENT ENDPOINTS ====================

    @GetMapping("/theater/{theaterId}/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_MANAGER')")
    public ResponseEntity<List<BookingSummaryDto>> getTheaterTodaysBookings(@PathVariable Long theaterId) {
        // This would return today's bookings for a specific theater
        return ResponseEntity.ok().build();
    }

    @GetMapping("/theater/{theaterId}/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_MANAGER')")
    public ResponseEntity<Object> getTheaterRevenue(
            @PathVariable Long theaterId,
            @RequestParam(required = false) String period) {

        // This would return revenue information for a theater
        return ResponseEntity.ok().build();
    }

    // ==================== VALIDATION ENDPOINTS ====================

    @GetMapping("/{bookingReference}/status")
    public ResponseEntity<Object> getBookingStatus(@PathVariable String bookingReference) {
        BookingResponseDto booking = bookingService.getBookingByReference(bookingReference);

        Object status = new Object() {
            public final String bookingReference = booking.getBookingReference();
            public final String status = booking.getStatus().toString();
            public final String paymentStatus = booking.getPaymentStatus().toString();
            public final Boolean canBeCancelled = booking.getCanBeCancelled();
            public final Boolean canBeModified = booking.getCanBeModified();
            public final Long timeToExpiryMinutes = booking.getTimeToExpiryMinutes();
            public final String showDetails = booking.getShow().getMovieTitle() + " at " +
                    booking.getShow().getTheaterName();
        };

        return ResponseEntity.ok(status);
    }

    @PostMapping("/{bookingReference}/extend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> extendBookingExpiry(
            @PathVariable String bookingReference,
            @RequestParam(defaultValue = "15") int minutes) {

        // This would extend the booking expiry time (admin only)
        log.info("Extending booking {} expiry by {} minutes", bookingReference, minutes);
        return ResponseEntity.ok("Booking expiry extended successfully");
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> bulkCancelBookings(
            @RequestBody List<String> bookingReferences,
            @RequestParam String reason) {

        // This would cancel multiple bookings at once (admin only)
        log.info("Bulk cancelling {} bookings", bookingReferences.size());
        return ResponseEntity.ok().build();
    }

    // ==================== HEALTH CHECK ====================

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Booking service is running");
    }

    // ==================== EXCEPTION HANDLING HELPERS ====================

    @ExceptionHandler(BookingService.BookingNotFoundException.class)
    public ResponseEntity<Object> handleBookingNotFound(BookingService.BookingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("BOOKING_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(BookingService.BookingExpiredException.class)
    public ResponseEntity<Object> handleBookingExpired(BookingService.BookingExpiredException e) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse("BOOKING_EXPIRED", e.getMessage()));
    }

    @ExceptionHandler(BookingService.BookingNotModifiableException.class)
    public ResponseEntity<Object> handleBookingNotModifiable(BookingService.BookingNotModifiableException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("BOOKING_NOT_MODIFIABLE", e.getMessage()));
    }

    @ExceptionHandler(BookingService.InvalidPaymentAmountException.class)
    public ResponseEntity<Object> handleInvalidPaymentAmount(BookingService.InvalidPaymentAmountException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PAYMENT_AMOUNT", e.getMessage()));
    }

    // Simple error response class
    public static class ErrorResponse {
        public final String code;
        public final String message;
        public final String timestamp;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
    }
}