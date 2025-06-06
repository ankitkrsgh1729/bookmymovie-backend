package com.bookmymovie.entity;
import com.bookmymovie.constants.TheaterConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shows",
        indexes = {
                @Index(name = "idx_show_screen", columnList = "screen_id"),
                @Index(name = "idx_show_movie", columnList = "movie_id"),
                @Index(name = "idx_show_date_time", columnList = "show_date, show_time"),
                @Index(name = "idx_show_status", columnList = "status"),
                @Index(name = "uk_show_screen_datetime", columnList = "screen_id, show_date, show_time", unique = true)
        })
@SQLDelete(sql = "UPDATE shows SET deleted = true WHERE show_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"screen", "movie", "bookings"})
@ToString(exclude = {"screen", "movie", "bookings"})
public class Show extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_id")
    private Long showId;

    @NotNull(message = "Movie is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull(message = "Screen is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @NotNull(message = "Show date is required")
    @FutureOrPresent(message = "Show date cannot be in the past")
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull(message = "Show status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ShowStatus status = ShowStatus.SCHEDULED;

    // Pricing information
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // Dynamic pricing factors
    @DecimalMin(value = "0.1", message = "Price multiplier must be at least 0.1")
    @DecimalMax(value = "5.0", message = "Price multiplier cannot exceed 5.0")
    @Column(name = "price_multiplier", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal priceMultiplier = BigDecimal.ONE;

    // Show metadata
    @Column(name = "language", length = 50)
    private String language; // Can override movie language

    @Column(name = "subtitle_language", length = 50)
    private String subtitleLanguage;

    @Column(name = "has_intermission")
    @Builder.Default
    private Boolean hasIntermission = true;

    @Column(name = "intermission_duration_minutes")
    @Builder.Default
    private Integer intermissionDurationMinutes = TheaterConstant.INTERMISSION_TIME_MINUTES;

    // Capacity management
    @PositiveOrZero(message = "Available seats cannot be negative")
    @Column(name = "available_seats")
    private Integer availableSeats;

    @PositiveOrZero(message = "Booked seats cannot be negative")
    @Column(name = "booked_seats")
    @Builder.Default
    private Integer bookedSeats = 0;

    @PositiveOrZero(message = "Blocked seats cannot be negative")
    @Column(name = "blocked_seats")
    @Builder.Default
    private Integer blockedSeats = 0;

    // Show features and special attributes
    @ElementCollection
    @CollectionTable(
            name = "show_features",
            joinColumns = @JoinColumn(name = "show_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "feature")
    @Builder.Default
    private List<ShowFeature> features = new ArrayList<>();

    // Booking cut-off times
    @Column(name = "booking_opens_at")
    private LocalDateTime bookingOpensAt;

    @Column(name = "booking_closes_at")
    private LocalDateTime bookingClosesAt;

    // Relationships
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    // Business logic methods
    public LocalDateTime getShowDateTime() {
        return LocalDateTime.of(showDate, showTime);
    }

    public LocalDateTime getEndDateTime() {
        return LocalDateTime.of(showDate, endTime);
    }

    public boolean isActive() {
        return status == ShowStatus.SCHEDULED || status == ShowStatus.RUNNING;
    }

    public boolean canBeBooked() {
        LocalDateTime now = LocalDateTime.now();
        return isActive() &&
                availableSeats > 0 &&
                (bookingOpensAt == null || now.isAfter(bookingOpensAt)) &&
                (bookingClosesAt == null || now.isBefore(bookingClosesAt)) &&
                now.isBefore(getShowDateTime());
    }

    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(getShowDateTime());
    }

    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(getEndDateTime());
    }

    public void updateSeatCounts() {
        // This would typically be called after booking operations
        int totalSeats = screen.getTotalSeats();
        this.availableSeats = totalSeats - bookedSeats - blockedSeats;
    }

    public BigDecimal getEffectivePrice(TheaterConstant.SeatCategory seatCategory) {
        BigDecimal categoryMultiplier = getSeatCategoryMultiplier(seatCategory);
        return basePrice.multiply(priceMultiplier).multiply(categoryMultiplier);
    }

    private BigDecimal getSeatCategoryMultiplier(TheaterConstant.SeatCategory category) {
        return switch (category) {
            case REGULAR -> BigDecimal.ONE;
            case PREMIUM -> new BigDecimal("1.3");
            case VIP -> new BigDecimal("1.8");
            case EXECUTIVE -> new BigDecimal("2.0");
            case BALCONY -> new BigDecimal("1.2");
            case BOX -> new BigDecimal("2.5");
            case WHEELCHAIR -> BigDecimal.ONE;
        };
    }

    public boolean isOverlapping(LocalTime otherStartTime, LocalTime otherEndTime) {
        return !(endTime.isBefore(otherStartTime) || showTime.isAfter(otherEndTime));
    }

    public boolean isWeekend() {
        return showDate.getDayOfWeek().getValue() >= 6; // Saturday or Sunday
    }

    public boolean isPeakTime() {
        // Evening shows (6 PM - 10 PM) are considered peak time
        return showTime.isAfter(LocalTime.of(18, 0)) &&
                showTime.isBefore(LocalTime.of(22, 0));
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setShow(this);
        this.bookedSeats += booking.getNumberOfSeats();
        updateSeatCounts();
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setShow(null);
        this.bookedSeats -= booking.getNumberOfSeats();
        updateSeatCounts();
    }

    public boolean hasFeature(ShowFeature feature) {
        return features.contains(feature);
    }

    public double getOccupancyPercentage() {
        int totalSeats = screen.getTotalSeats();
        return totalSeats > 0 ? (double) bookedSeats / totalSeats * 100 : 0;
    }

    // Validation methods
    public void validateShowTiming() {
        if (showTime.isAfter(endTime)) {
            throw new IllegalStateException("Show start time cannot be after end time");
        }

        if (showTime.equals(endTime)) {
            throw new IllegalStateException("Show start time and end time cannot be the same");
        }
    }

    public void validateBookingWindow() {
        if (bookingOpensAt != null && bookingClosesAt != null) {
            if (bookingOpensAt.isAfter(bookingClosesAt)) {
                throw new IllegalStateException("Booking open time cannot be after booking close time");
            }
        }

        if (bookingClosesAt != null && bookingClosesAt.isAfter(getShowDateTime())) {
            throw new IllegalStateException("Booking close time cannot be after show start time");
        }
    }

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    // Show Status enum
    public enum ShowStatus {
        SCHEDULED("Show is scheduled and bookings are open"),
        RUNNING("Show is currently running"),
        COMPLETED("Show has been completed"),
        CANCELLED("Show has been cancelled"),
        POSTPONED("Show has been postponed"),
        HOUSEFULL("Show is fully booked");

        private final String description;

        ShowStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Show Features enum
    public enum ShowFeature {
        EARLY_BIRD_DISCOUNT("Early bird booking discount available"),
        GROUP_DISCOUNT("Group booking discount available"),
        STUDENT_DISCOUNT("Student discount available"),
        SENIOR_CITIZEN_DISCOUNT("Senior citizen discount available"),
        SPECIAL_SCREENING("Special screening event"),
        PREMIERE("Movie premiere show"),
        LATE_NIGHT_SHOW("Late night show"),
        MATINEE_SHOW("Matinee/afternoon show"),
        FESTIVAL_SCREENING("Film festival screening"),
        LIVE_TELECAST("Live telecast event");

        private final String description;

        ShowFeature(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}