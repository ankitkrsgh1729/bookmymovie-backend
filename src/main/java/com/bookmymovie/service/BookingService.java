package com.bookmymovie.service;

import com.bookmymovie.constants.TheaterConstant;
import com.bookmymovie.dto.request.*;
import com.bookmymovie.dto.response.*;
import com.bookmymovie.entity.*;
import com.bookmymovie.exception.BaseException;
import com.bookmymovie.repository.BookingRepository;
import com.bookmymovie.repository.SeatRepository;
import com.bookmymovie.repository.ShowRepository;
import com.bookmymovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final MockPaymentService paymentService;

    // Business configuration
    private static final BigDecimal CONVENIENCE_FEE_RATE = BigDecimal.valueOf(0.05); // 5%
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.18); // 18% GST
    private static final int BOOKING_EXPIRY_MINUTES = 15;

    // ==================== CORE BOOKING OPERATIONS ====================

    public BookingInitiationResponseDto initiateBooking(BookingCreateRequestDto request) {
        log.info("Initiating booking for show {} with {} seats", request.getShowId(), request.getSeatIds().size());

        try {
            // Validate booking request
            BookingValidationResponseDto validation = validateBookingRequest(request);
            if (!validation.getValid()) {
                return BookingInitiationResponseDto.builder()
                        .success(false)
                        .message("Booking validation failed")
                        .errors(validation.getErrors())
                        .build();
            }

            // Load entities
            Show show = findShowById(request.getShowId());
            User user = findUserById(request.getUserId());
            List<Seat> seats = findSeatsByIds(request.getSeatIds());

            // Calculate pricing
            BookingPricingResponseDto pricing = calculateBookingPricing(
                    BookingPricingRequestDto.builder()
                            .showId(request.getShowId())
                            .seatIds(request.getSeatIds())
                            .couponCode(request.getCouponCode())
                            .build()
            );

            // Create booking entity
            Booking booking = createBookingEntity(request, show, user, seats, pricing);
            booking = bookingRepository.save(booking);

            // Reserve seats temporarily
            reserveSeats(booking, seats);

            log.info("Booking initiated successfully with reference: {}", booking.getBookingReference());

            return BookingInitiationResponseDto.builder()
                    .success(true)
                    .message("Booking initiated successfully")
                    .bookingReference(booking.getBookingReference())
                    .bookingId(booking.getBookingId())
                    .expiryTime(booking.getExpiryTime())
                    .timeToExpiryMinutes(booking.getTimeToExpiry())
                    .finalAmount(booking.getFinalAmount())
                    .build();

        } catch (Exception e) {
            log.error("Error initiating booking", e);
            return BookingInitiationResponseDto.builder()
                    .success(false)
                    .message("Failed to initiate booking: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment for booking: {}", request.getBookingReference());

        Booking booking = findBookingByReference(request.getBookingReference());

        // Validate booking state
        if (!booking.canBeModified()) {
            throw new BookingNotModifiableException("Booking cannot be modified at this time");
        }

        if (booking.isExpired()) {
            booking.expireBooking();
            bookingRepository.save(booking);
            throw new BookingExpiredException("Booking has expired");
        }

        // Validate payment amount
        if (request.getPaymentAmount().compareTo(booking.getFinalAmount()) != 0) {
            throw new InvalidPaymentAmountException("Payment amount does not match booking amount");
        }

        // Process payment through mock service
        PaymentResponseDto paymentResponse = paymentService.processPaymentByMethod(request);

        // Update booking with payment result
        booking.updatePaymentStatus(
                paymentResponse.getPaymentStatus(),
                paymentResponse.getPaymentReference(),
                paymentResponse.getGatewayResponse()
        );

        if (paymentResponse.getSuccess()) {
            booking.setPaymentMethod(request.getPaymentMethod());
            booking.confirmBooking();

            // Update show seat count
            updateShowSeatCount(booking.getShow(), booking.getNumberOfSeats(), true);

            log.info("Payment successful for booking: {}", booking.getBookingReference());
        } else {
            log.warn("Payment failed for booking: {}", booking.getBookingReference());
        }

        bookingRepository.save(booking);
        return paymentResponse;
    }

    public BookingConfirmationResponseDto confirmBooking(String bookingReference) {
        log.info("Confirming booking: {}", bookingReference);

        Booking booking = findBookingByReference(bookingReference);

        if (!booking.isPaymentCompleted()) {
            throw new PaymentNotCompletedException("Payment must be completed before confirmation");
        }

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            log.info("Booking already confirmed: {}", bookingReference);
        } else {
            booking.confirmBooking();
            bookingRepository.save(booking);
        }

        // Generate receipt
        BookingReceiptDto receipt = generateBookingReceipt(booking);
        String qrCode = generateBookingQrCode(booking);

        return BookingConfirmationResponseDto.builder()
                .bookingReference(bookingReference)
                .confirmed(true)
                .message("Booking confirmed successfully")
                .confirmationTime(LocalDateTime.now())
                .receipt(receipt)
                .qrCode(qrCode)
                .nextSteps(List.of(
                        "Save this confirmation for your records",
                        "Arrive at the theater 30 minutes before show time",
                        "Present this QR code at the entrance",
                        "Enjoy your movie!"
                ))
                .build();
    }

    // ==================== BOOKING MANAGEMENT ====================

    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId) {
        Booking booking = findBookingById(bookingId);
        return mapToBookingResponseDto(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByReference(String bookingReference) {
        Booking booking = findBookingByReference(bookingReference);
        return mapToBookingResponseDto(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingSummaryDto> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserUserIdOrderByBookingDateDesc(userId);
        return bookings.stream()
                .map(this::mapToBookingSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BookingSummaryDto> getUserBookingsPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingDate"));
        Page<Booking> bookings = bookingRepository.findByUserUserIdOrderByBookingDateDesc(userId, pageable);
        return bookings.map(this::mapToBookingSummaryDto);
    }

    public BookingResponseDto updateBooking(Long bookingId, BookingUpdateRequestDto request) {
        log.info("Updating booking: {}", bookingId);

        Booking booking = findBookingById(bookingId);

        if (!booking.canBeModified()) {
            throw new BookingNotModifiableException("Booking cannot be modified at this time");
        }

        // Update modifiable fields
        if (request.getContactEmail() != null) {
            booking.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            booking.setContactPhone(request.getContactPhone());
        }
        if (request.getSpecialRequests() != null) {
            booking.setSpecialRequests(request.getSpecialRequests());
        }
        if (request.getStatus() != null && isValidStatusTransition(booking.getStatus(), request.getStatus())) {
            booking.setStatus(request.getStatus());
        }

        booking = bookingRepository.save(booking);
        log.info("Booking updated successfully: {}", bookingId);

        return mapToBookingResponseDto(booking);
    }

    // ==================== CANCELLATION & REFUNDS ====================

    public BookingCancellationResponseDto cancelBooking(BookingCancellationRequestDto request) {
        log.info("Cancelling booking: {}", request.getBookingReference());

        Booking booking = findBookingByReference(request.getBookingReference());

        if (!booking.canBeCancelled()) {
            throw new BookingNotCancellableException("Booking cannot be cancelled at this time");
        }

        // Cancel booking
        booking.cancelBooking(request.getReason());

        // Release seats
        updateShowSeatCount(booking.getShow(), booking.getNumberOfSeats(), false);

        BigDecimal refundAmount = BigDecimal.ZERO;
        String refundReference = null;
        LocalDateTime expectedRefundDate = null;

        // Process refund if requested and payment was completed
        if (request.getRequestRefund() && booking.isPaymentCompleted()) {
            refundAmount = calculateRefundAmount(booking);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                PaymentResponseDto refundResponse = paymentService.refundPayment(
                        booking.getPaymentReference(), refundAmount, request.getReason());

                if (refundResponse.getSuccess()) {
                    booking.processRefund(refundAmount);
                    refundReference = refundResponse.getPaymentReference();
                    expectedRefundDate = LocalDateTime.now().plusDays(5); // 5 business days
                }
            }
        }

        bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", request.getBookingReference());

        return BookingCancellationResponseDto.builder()
                .bookingReference(request.getBookingReference())
                .cancelled(true)
                .message("Booking cancelled successfully")
                .cancellationTime(booking.getCancellationDate())
                .refundAmount(refundAmount)
                .refundReference(refundReference)
                .expectedRefundDate(expectedRefundDate)
                .refundDetails(generateRefundDetails(booking, refundAmount))
                .build();
    }

    // ==================== PRICING OPERATIONS ====================

    @Transactional(readOnly = true)
    public BookingPricingResponseDto calculateBookingPricing(BookingPricingRequestDto request) {
        Show show = findShowById(request.getShowId());
        List<Seat> seats = findSeatsByIds(request.getSeatIds());

        // Calculate base amount from seat prices
        BigDecimal baseAmount = seats.stream()
                .map(seat -> seat.getBasePrice() != null ? seat.getBasePrice() : show.calculateActualPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply show pricing multipliers
        BigDecimal showMultipliedAmount = baseAmount.multiply(getShowPricingMultiplier(show));

        // Calculate fees and taxes
        BigDecimal convenienceFee = showMultipliedAmount.multiply(CONVENIENCE_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal amountBeforeTax = showMultipliedAmount.add(convenienceFee);
        BigDecimal taxes = amountBeforeTax.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Apply discounts (coupon logic would go here)
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean couponApplied = false;

        if (request.getCouponCode() != null) {
            // Mock coupon validation and discount calculation
            discountAmount = amountBeforeTax.multiply(BigDecimal.valueOf(0.10)) // 10% discount
                    .setScale(2, RoundingMode.HALF_UP);
            couponApplied = true;
        }

        BigDecimal finalAmount = amountBeforeTax.add(taxes).subtract(discountAmount);

        // Build seat pricing details
        List<BookingPricingResponseDto.SeatPricingDto> seatPricing = seats.stream()
                .map(seat -> BookingPricingResponseDto.SeatPricingDto.builder()
                        .seatId(seat.getSeatId())
                        .seatIdentifier(seat.getRowLabel() + seat.getSeatNumber())
                        .category(seat.getCategory().toString())
                        .basePrice(seat.getBasePrice() != null ? seat.getBasePrice() : show.getBasePrice())
                        .finalPrice(show.calculateActualPrice())
                        .build())
                .collect(Collectors.toList());

        // Build pricing breakdown
        List<BookingPricingResponseDto.PricingBreakdownDto> breakdown = List.of(
                BookingPricingResponseDto.PricingBreakdownDto.builder()
                        .component("Base Amount")
                        .amount(baseAmount)
                        .description("Base ticket price for " + seats.size() + " seats")
                        .build(),
                BookingPricingResponseDto.PricingBreakdownDto.builder()
                        .component("Show Pricing")
                        .amount(showMultipliedAmount.subtract(baseAmount))
                        .description("Time-based pricing adjustments")
                        .build(),
                BookingPricingResponseDto.PricingBreakdownDto.builder()
                        .component("Convenience Fee")
                        .amount(convenienceFee)
                        .description("Platform convenience charges")
                        .build(),
                BookingPricingResponseDto.PricingBreakdownDto.builder()
                        .component("Taxes (GST)")
                        .amount(taxes)
                        .description("Government taxes")
                        .build(),
                BookingPricingResponseDto.PricingBreakdownDto.builder()
                        .component("Discount")
                        .amount(discountAmount.negate())
                        .description(couponApplied ? "Coupon discount applied" : "No discount")
                        .build()
        );

        return BookingPricingResponseDto.builder()
                .showId(request.getShowId())
                .numberOfSeats(seats.size())
                .baseAmount(baseAmount)
                .convenienceFee(convenienceFee)
                .taxes(taxes)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .seatPricing(seatPricing)
                .pricingBreakdown(breakdown)
                .couponApplied(couponApplied)
                .couponCode(request.getCouponCode())
                .couponDiscount(couponApplied ? discountAmount : BigDecimal.ZERO)
                .build();
    }

    // ==================== VALIDATION OPERATIONS ====================

    @Transactional(readOnly = true)
    public BookingValidationResponseDto validateBookingRequest(BookingCreateRequestDto request) {
        List<String> errors = new java.util.ArrayList<>();
        List<String> warnings = new java.util.ArrayList<>();

        try {
            // Validate show
            Show show = findShowById(request.getShowId());
            if (!show.canBeBooked()) {
                errors.add("Show is not available for booking");
            }

            // Validate seats
            List<Seat> seats = findSeatsByIds(request.getSeatIds());
            if (seats.size() != request.getSeatIds().size()) {
                errors.add("Some seats are invalid or not found");
            }

            // Check seat availability
            List<Long> bookedSeatIds = bookingRepository.getBookedSeatIdsForShow(request.getShowId());
            List<Long> unavailableSeats = request.getSeatIds().stream()
                    .filter(bookedSeatIds::contains)
                    .collect(Collectors.toList());

            if (!unavailableSeats.isEmpty()) {
                errors.add("Some seats are already booked: " + unavailableSeats);
            }

            // Validate user
            User user = findUserById(request.getUserId());
            if (user == null) {
                errors.add("Invalid user");
            }

            // Business rule validations
            if (request.getSeatIds().size() > 10) {
                errors.add("Cannot book more than 10 seats at once");
            }

            if (show.getShowDateTime().isBefore(LocalDateTime.now().plusMinutes(30))) {
                warnings.add("Show starts within 30 minutes");
            }

            return BookingValidationResponseDto.builder()
                    .valid(errors.isEmpty())
                    .message(errors.isEmpty() ? "Validation successful" : "Validation failed")
                    .errors(errors)
                    .warnings(warnings)
                    .seatsAvailable(unavailableSeats.isEmpty())
                    .showBookable(show.canBeBooked())
                    .userEligible(user != null)
                    .estimatedAmount(errors.isEmpty() ? calculateEstimatedAmount(show, seats) : BigDecimal.ZERO)
                    .build();

        } catch (Exception e) {
            log.error("Error validating booking request", e);
            errors.add("Validation error: " + e.getMessage());
            return BookingValidationResponseDto.builder()
                    .valid(false)
                    .message("Validation error")
                    .errors(errors)
                    .build();
        }
    }

    // ==================== SEAT AVAILABILITY ====================

    @Transactional(readOnly = true)
    public SeatAvailabilityResponseDto getSeatAvailability(SeatAvailabilityRequestDto request) {
        Show show = findShowById(request.getShowId());
        List<Seat> allSeats = seatRepository.findByScreenScreenIdOrderByRowLabelAscSeatNumberAsc(
                show.getScreen().getScreenId());

        List<Long> bookedSeatIds = bookingRepository.getBookedSeatIdsForShow(request.getShowId());

        List<SeatAvailabilityResponseDto.SeatMapDto> seatMap = allSeats.stream()
                .map(seat -> SeatAvailabilityResponseDto.SeatMapDto.builder()
                        .seatId(seat.getSeatId())
                        .rowLabel(seat.getRowLabel())
                        .seatNumber(seat.getSeatNumber())
                        .category(seat.getCategory().toString())
                        .price(seat.getBasePrice() != null ? seat.getBasePrice() : show.calculateActualPrice())
                        .available(!bookedSeatIds.contains(seat.getSeatId()) && seat.getStatus() == TheaterConstant.SeatStatus.AVAILABLE)
                        .blocked(seat.getStatus() == TheaterConstant.SeatStatus.UNDER_MAINTENANCE)
                        .seatType(seat.getSeatType().toString())
                        .features(seat.getFeatures().stream().map(Enum::toString).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return SeatAvailabilityResponseDto.builder()
                .showId(request.getShowId())
                .movieTitle(show.getMovie().getTitle())
                .theaterName(show.getScreen().getTheater().getName())
                .screenName(show.getScreen().getName())
                .showDateTime(show.getShowDateTime())
                .totalSeats(allSeats.size())
                .availableSeats(show.getAvailableSeats())
                .bookedSeats(show.getBookedSeats())
                .seatMap(seatMap)
                .basePrice(show.getBasePrice())
                .build();
    }

    // ==================== SCHEDULED TASKS ====================

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void expireOldBookings() {
        log.info("Running expired booking cleanup job");

        LocalDateTime cutoffTime = LocalDateTime.now();
        int expiredCount = bookingRepository.expireOldPendingBookings(cutoffTime);

        if (expiredCount > 0) {
            log.info("Expired {} old pending bookings", expiredCount);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void markCompletedShows() {
        log.info("Running completed show booking update job");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        List<Booking> completedBookings = bookingRepository.findCompletedShowBookings(cutoffTime);

        completedBookings.forEach(booking -> {
            if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                booking.setStatus(Booking.BookingStatus.COMPLETED);
            }
        });

        if (!completedBookings.isEmpty()) {
            bookingRepository.saveAll(completedBookings);
            log.info("Marked {} bookings as completed", completedBookings.size());
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Booking createBookingEntity(BookingCreateRequestDto request, Show show, User user,
                                        List<Seat> seats, BookingPricingResponseDto pricing) {

        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .bookingDate(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(BOOKING_EXPIRY_MINUTES))
                .numberOfSeats(seats.size())
                .totalAmount(pricing.getBaseAmount())
                .discountAmount(pricing.getDiscountAmount())
                .convenienceFee(pricing.getConvenienceFee())
                .taxes(pricing.getTaxes())
                .finalAmount(pricing.getFinalAmount())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .specialRequests(request.getSpecialRequests())
                .source(request.getSource() != null ? request.getSource() : Booking.BookingSource.WEB)
                .build();

        // Create booking seats
        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seat)
                        .seatPrice(seat.getBasePrice() != null ? seat.getBasePrice() : show.calculateActualPrice())
                        .seatCategory(seat.getCategory().toString())
                        .seatRow(seat.getRowLabel())
                        .seatNumber(seat.getSeatNumber())
                        .build())
                .collect(Collectors.toList());

        booking.setBookedSeats(bookingSeats);
        return booking;
    }

    private void reserveSeats(Booking booking, List<Seat> seats) {
        // In a real system, you might update seat status to TEMPORARILY_RESERVED
        // For now, we'll just update the show's booked seat count
        updateShowSeatCount(booking.getShow(), seats.size(), true);
    }

    private void updateShowSeatCount(Show show, int seatCount, boolean isBooking) {
        if (isBooking) {
            show.bookSeats(seatCount);
        } else {
            show.releaseSeats(seatCount);
        }
        showRepository.save(show);
    }

    private BigDecimal getShowPricingMultiplier(Show show) {
        BigDecimal multiplier = BigDecimal.ONE;

        if (show.isWeekend()) {
            multiplier = multiplier.multiply(show.getWeekendMultiplier());
        }
        if (show.isPrimeTime()) {
            multiplier = multiplier.multiply(show.getPrimeTimeMultiplier());
        }
        if (show.getIsHoliday()) {
            multiplier = multiplier.multiply(show.getHolidayMultiplier());
        }

        return multiplier;
    }

    private BigDecimal calculateEstimatedAmount(Show show, List<Seat> seats) {
        return seats.stream()
                .map(seat -> seat.getBasePrice() != null ? seat.getBasePrice() : show.calculateActualPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateRefundAmount(Booking booking) {
        // Calculate refund based on cancellation time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime showTime = booking.getShow().getShowDateTime();

        long hoursToShow = java.time.Duration.between(now, showTime).toHours();

        if (hoursToShow >= 24) {
            // Full refund for cancellations 24+ hours before
            return booking.getFinalAmount();
        } else if (hoursToShow >= 2) {
            // 80% refund for cancellations 2-24 hours before
            return booking.getFinalAmount().multiply(BigDecimal.valueOf(0.80));
        } else {
            // No refund for cancellations within 2 hours
            return BigDecimal.ZERO;
        }
    }

    private List<String> generateRefundDetails(Booking booking, BigDecimal refundAmount) {
        List<String> details = new java.util.ArrayList<>();
        details.add("Original amount: ₹" + booking.getFinalAmount());
        details.add("Refund amount: ₹" + refundAmount);
        details.add("Refund will be credited to original payment method");
        details.add("Processing time: 3-5 business days");
        return details;
    }

    private boolean isValidStatusTransition(Booking.BookingStatus from, Booking.BookingStatus to) {
        // Define valid status transitions
        return switch (from) {
            case PENDING -> to == Booking.BookingStatus.CONFIRMED || to == Booking.BookingStatus.CANCELLED || to == Booking.BookingStatus.EXPIRED;
            case CONFIRMED -> to == Booking.BookingStatus.CANCELLED || to == Booking.BookingStatus.COMPLETED || to == Booking.BookingStatus.NO_SHOW;
            case CANCELLED, EXPIRED, COMPLETED, NO_SHOW -> false; // Terminal states
        };
    }

    private BookingReceiptDto generateBookingReceipt(Booking booking) {
        List<String> seatNumbers = booking.getBookedSeats().stream()
                .map(BookingSeat::getSeatIdentifier)
                .collect(Collectors.toList());

        return BookingReceiptDto.builder()
                .bookingReference(booking.getBookingReference())
                .bookingDate(booking.getBookingDate())
                .paymentDate(booking.getPaymentDate())
                .customerName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName())
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .movieLanguage(booking.getShow().getMovie().getLanguage().toString())
                .movieRating(booking.getShow().getMovie().getRating())
                .movieDuration(booking.getShow().getMovie().getDurationMinutes())
                .theaterName(booking.getShow().getScreen().getTheater().getName())
                .theaterAddress(booking.getShow().getScreen().getTheater().getAddress())
                .screenName(booking.getShow().getScreen().getName())
                .showDate(booking.getShow().getShowDate())
                .showTime(booking.getShow().getShowTime())
                .numberOfSeats(booking.getNumberOfSeats())
                .seatNumbers(seatNumbers)
                .ticketPrice(booking.getTotalAmount())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .convenienceFee(booking.getConvenienceFee())
                .taxes(booking.getTaxes())
                .finalAmount(booking.getFinalAmount())
                .paymentMethod(booking.getPaymentMethod() != null ? booking.getPaymentMethod().getDisplayName() : "")
                .paymentReference(booking.getPaymentReference())
                .specialRequests(booking.getSpecialRequests())
                .qrCode(generateBookingQrCode(booking))
                .importantNotes("Please arrive 30 minutes before show time. This ticket is non-transferable.")
                .build();
    }

    private String generateBookingQrCode(Booking booking) {
        // Mock QR code generation - would use actual QR library in production
        return "QR:" + booking.getBookingReference() + ":" + booking.getShow().getShowId() + ":" + booking.getNumberOfSeats();
    }

    private BookingResponseDto mapToBookingResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .bookingId(booking.getBookingId())
                .bookingReference(booking.getBookingReference())
                .bookingDate(booking.getBookingDate())
                .expiryTime(booking.getExpiryTime())
                .numberOfSeats(booking.getNumberOfSeats())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .convenienceFee(booking.getConvenienceFee())
                .taxes(booking.getTaxes())
                .finalAmount(booking.getFinalAmount())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .paymentReference(booking.getPaymentReference())
                .paymentDate(booking.getPaymentDate())
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .specialRequests(booking.getSpecialRequests())
                .source(booking.getSource())
                .cancellationReason(booking.getCancellationReason())
                .cancellationDate(booking.getCancellationDate())
                .refundAmount(booking.getRefundAmount())
                .refundDate(booking.getRefundDate())
                .timeToExpiryMinutes(booking.getTimeToExpiry())
                .canBeCancelled(booking.canBeCancelled())
                .canBeModified(booking.canBeModified())
                .user(mapToUserDetailsDto(booking.getUser()))
                .show(mapToShowDetailsDto(booking.getShow()))
                .seats(booking.getBookedSeats().stream()
                        .map(this::mapToSeatDetailsDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private BookingSummaryDto mapToBookingSummaryDto(Booking booking) {
        String seatNumbers = booking.getBookedSeats().stream()
                .map(BookingSeat::getSeatIdentifier)
                .collect(Collectors.joining(", "));

        return BookingSummaryDto.builder()
                .bookingId(booking.getBookingId())
                .bookingReference(booking.getBookingReference())
                .bookingDate(booking.getBookingDate())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .theaterName(booking.getShow().getScreen().getTheater().getName())
                .showDate(booking.getShow().getShowDate())
                .showTime(booking.getShow().getShowTime())
                .numberOfSeats(booking.getNumberOfSeats())
                .finalAmount(booking.getFinalAmount())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .canBeCancelled(booking.canBeCancelled())
                .seatNumbers(seatNumbers)
                .build();
    }

    private BookingResponseDto.UserDetailsDto mapToUserDetailsDto(User user) {
        return BookingResponseDto.UserDetailsDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private BookingResponseDto.ShowDetailsDto mapToShowDetailsDto(Show show) {
        return BookingResponseDto.ShowDetailsDto.builder()
                .showId(show.getShowId())
                .showDate(show.getShowDate())
                .showTime(show.getShowTime())
                .showDateTime(show.getShowDateTime())
                .movieTitle(show.getMovie().getTitle())
                .movieLanguage(show.getMovie().getLanguage().toString())
                .movieRating(show.getMovie().getRating())
                .movieDuration(show.getMovie().getDurationMinutes())
                .theaterName(show.getScreen().getTheater().getName())
                .theaterAddress(show.getScreen().getTheater().getAddress())
                .theaterCity(show.getScreen().getTheater().getCity())
                .screenName(show.getScreen().getName())
                .screenType(show.getScreen().getScreenType().toString())
                .showPrice(show.calculateActualPrice())
                .build();
    }

    private BookingResponseDto.SeatDetailsDto mapToSeatDetailsDto(BookingSeat bookingSeat) {
        return BookingResponseDto.SeatDetailsDto.builder()
                .seatId(bookingSeat.getSeat().getSeatId())
                .rowLabel(bookingSeat.getSeatRow())
                .seatNumber(bookingSeat.getSeatNumber())
                .category(bookingSeat.getSeatCategory())
                .price(bookingSeat.getSeatPrice())
                .seatIdentifier(bookingSeat.getSeatIdentifier())
                .build();
    }

    private Show findShowById(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with ID: " + showId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private List<Seat> findSeatsByIds(List<Long> seatIds) {
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new SeatNotFoundException("Some seats not found");
        }
        return seats;
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));
    }

    private Booking findBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with reference: " + bookingReference));
    }

    // ==================== CUSTOM EXCEPTIONS ====================

    public static class BookingNotFoundException extends BaseException {
        public BookingNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class ShowNotFoundException extends BaseException {
        public ShowNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class UserNotFoundException extends BaseException {
        public UserNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class SeatNotFoundException extends BaseException {
        public SeatNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class BookingNotModifiableException extends BaseException {
        public BookingNotModifiableException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 422;
        }
    }

    public static class BookingExpiredException extends BaseException {
        public BookingExpiredException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 410; // Gone
        }
    }

    public static class InvalidPaymentAmountException extends BaseException {
        public InvalidPaymentAmountException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 400;
        }
    }

    public static class PaymentNotCompletedException extends BaseException {
        public PaymentNotCompletedException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 422;
        }
    }

    public static class BookingNotCancellableException extends BaseException {
        public BookingNotCancellableException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 422;
        }
    }
}