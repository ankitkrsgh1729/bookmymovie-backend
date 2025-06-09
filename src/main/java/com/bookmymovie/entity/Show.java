package com.bookmymovie.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(name = "shows",
        indexes = {
                @Index(name = "idx_show_movie", columnList = "movie_id"),
                @Index(name = "idx_show_screen", columnList = "screen_id"),
                @Index(name = "idx_show_date_time", columnList = "show_date, show_time"),
                @Index(name = "idx_show_status", columnList = "status"),
                @Index(name = "idx_show_screen_datetime", columnList = "screen_id, show_date, show_time")
        })
@SQLDelete(sql = "UPDATE shows SET deleted = true WHERE show_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"movie", "screen"})
@ToString(exclude = {"movie", "screen"})
public class Show extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_id")
    private Long showId;

    // ADD THIS: Optimistic Locking Version Field
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @NotNull(message = "Movie is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull(message = "Screen is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @NotNull(message = "Show date is required")
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    @NotNull(message = "Show date time is required")
    @Column(name = "show_date_time", nullable = false)
    private LocalDateTime showDateTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Pricing
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "weekend_multiplier", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal weekendMultiplier = BigDecimal.valueOf(1.5);

    @Column(name = "prime_time_multiplier", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal primeTimeMultiplier = BigDecimal.valueOf(1.8);

    @Column(name = "holiday_multiplier", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal holidayMultiplier = BigDecimal.valueOf(2.0);

    // Show Status
    @NotNull(message = "Show status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ShowStatus status = ShowStatus.SCHEDULED;

    // Booking Information
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "booked_seats", nullable = false)
    @Builder.Default
    private Integer bookedSeats = 0;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    // Show Configuration
    @Column(name = "has_intermission")
    @Builder.Default
    private Boolean hasIntermission = true;

    @Column(name = "intermission_duration_minutes")
    @Builder.Default
    private Integer intermissionDurationMinutes = 15;

    @Column(name = "cleaning_time_minutes")
    @Builder.Default
    private Integer cleaningTimeMinutes = 20;

    // Special Attributes
    @Column(name = "is_special_screening")
    @Builder.Default
    private Boolean isSpecialScreening = false;

    @Column(name = "is_premiere")
    @Builder.Default
    private Boolean isPremiere = false;

    @Column(name = "is_holiday")
    @Builder.Default
    private Boolean isHoliday = false;

    @Size(max = 500, message = "Special notes cannot exceed 500 characters")
    @Column(name = "special_notes", length = 500)
    private String specialNotes;

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    // Business Logic Methods

    @PrePersist
    @PreUpdate
    private void calculateDerivedFields() {
        // Set show date time
        if (showDate != null && showTime != null) {
            this.showDateTime = LocalDateTime.of(showDate, showTime);
        }

        // Calculate end time
        if (showDateTime != null && movie != null) {
            int totalDuration = movie.getDurationMinutes();
            if (hasIntermission) {
                totalDuration += intermissionDurationMinutes;
            }
            this.endTime = showDateTime.plusMinutes(totalDuration);
        }

        // Set total seats from screen
        if (screen != null && totalSeats == null) {
            this.totalSeats = screen.getTotalSeats();
        }

        // Calculate available seats
        if (totalSeats != null && bookedSeats != null) {
            this.availableSeats = totalSeats - bookedSeats;
        }
    }

    public BigDecimal calculateActualPrice() {
        BigDecimal actualPrice = basePrice;

        // Apply weekend multiplier
        if (isWeekend()) {
            actualPrice = actualPrice.multiply(weekendMultiplier);
        }

        // Apply prime time multiplier
        if (isPrimeTime()) {
            actualPrice = actualPrice.multiply(primeTimeMultiplier);
        }

        // Apply holiday multiplier
        if (isHoliday) {
            actualPrice = actualPrice.multiply(holidayMultiplier);
        }

        // Apply special screening multiplier
        if (isSpecialScreening) {
            actualPrice = actualPrice.multiply(BigDecimal.valueOf(1.3));
        }

        // Apply premiere multiplier
        if (isPremiere) {
            actualPrice = actualPrice.multiply(BigDecimal.valueOf(2.5));
        }

        return actualPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean isWeekend() {
        DayOfWeek dayOfWeek = showDate.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public boolean isPrimeTime() {
        // Prime time: 6 PM to 10 PM
        return showTime.isAfter(LocalTime.of(18, 0)) &&
                showTime.isBefore(LocalTime.of(22, 0));
    }

    public boolean isMatineeShow() {
        // Morning shows: 9 AM to 12 PM
        return showTime.isAfter(LocalTime.of(9, 0)) &&
                showTime.isBefore(LocalTime.of(12, 0));
    }

    public boolean isLateNightShow() {
        // Late night: After 10 PM
        return showTime.isAfter(LocalTime.of(22, 0));
    }

    public LocalDateTime getActualEndTime() {
        return endTime.plusMinutes(cleaningTimeMinutes);
    }

    public boolean canBeBooked() {
        return status == ShowStatus.SCHEDULED &&
                LocalDateTime.now().isBefore(showDateTime) &&
                availableSeats > 0;
    }

    public boolean canBeCancelled() {
        return (status == ShowStatus.SCHEDULED) &&
                LocalDateTime.now().isBefore(showDateTime.minusHours(2));
    }

    public void bookSeats(int seatsToBook) {
        if (seatsToBook <= 0) {
            throw new IllegalArgumentException("Number of seats to book must be positive");
        }
        if (seatsToBook > availableSeats) {
            throw new IllegalStateException("Not enough available seats");
        }
        this.bookedSeats += seatsToBook;
        this.availableSeats -= seatsToBook;
    }

    public void releaseSeats(int seatsToRelease) {
        if (seatsToRelease <= 0) {
            throw new IllegalArgumentException("Number of seats to release must be positive");
        }
        if (seatsToRelease > bookedSeats) {
            throw new IllegalStateException("Cannot release more seats than booked");
        }
        this.bookedSeats -= seatsToRelease;
        this.availableSeats += seatsToRelease;
    }

    public double getOccupancyPercentage() {
        if (totalSeats == 0) return 0.0;
        return (bookedSeats * 100.0) / totalSeats;
    }

    public BigDecimal getProjectedRevenue() {
        return calculateActualPrice().multiply(BigDecimal.valueOf(bookedSeats));
    }

    public boolean isFullyBooked() {
        return availableSeats <= 0;
    }

    public boolean isStartingSoon() {
        // Starting within next 30 minutes
        return LocalDateTime.now().isAfter(showDateTime.minusMinutes(30)) &&
                LocalDateTime.now().isBefore(showDateTime);
    }

    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(showDateTime);
    }

    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    public void startShow() {
        if (status != ShowStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled shows can be started");
        }
        this.status = ShowStatus.ONGOING;
    }

    public void completeShow() {
        if (status != ShowStatus.ONGOING) {
            throw new IllegalStateException("Only ongoing shows can be completed");
        }
        this.status = ShowStatus.COMPLETED;
    }

    public void cancelShow(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Show cannot be cancelled at this time");
        }
        this.status = ShowStatus.CANCELLED;
        this.specialNotes = "Cancelled: " + reason;
    }

    public String getDisplayTime() {
        return showTime.toString();
    }

    public String getFormattedShowInfo() {
        return String.format("%s - %s at %s (%s)",
                movie.getTitle(),
                screen.getTheater().getName(),
                showTime,
                screen.getName());
    }

    // Show Status Enum
    public enum ShowStatus {
        SCHEDULED("Show is scheduled and can be booked"),
        ONGOING("Show is currently running"),
        COMPLETED("Show has completed successfully"),
        CANCELLED("Show has been cancelled"),
        POSTPONED("Show has been postponed to a later time"),
        HOUSEFULL("Show is fully booked");

        private final String description;

        ShowStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}