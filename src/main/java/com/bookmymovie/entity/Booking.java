package com.bookmymovie.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_user", columnList = "user_id"),
                @Index(name = "idx_booking_show", columnList = "show_id"),
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_date", columnList = "booking_date"),
                @Index(name = "uk_booking_reference", columnList = "booking_reference", unique = true)
        })
@SQLDelete(sql = "UPDATE bookings SET deleted = true WHERE booking_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"user", "show", "bookedSeats"})
@ToString(exclude = {"user", "show", "bookedSeats"})
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @NotBlank(message = "Booking reference is required")
    @Column(name = "booking_reference", nullable = false, unique = true, length = 20)
    private String bookingReference;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Show is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @NotNull(message = "Booking date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @NotNull(message = "Number of seats is required")
    @Positive(message = "Number of seats must be positive")
    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Booking status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // Payment information
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Contact information
    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    @Column(name = "contact_phone", nullable = false, length = 10)
    private String contactPhone;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Column(name = "contact_email", nullable = false, length = 100)
    private String contactEmail;

    // Booking timestamps
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Additional information
    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    // Relationships
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookedSeat> bookedSeats = new ArrayList<>();

    // Business logic methods
    public void addBookedSeat(BookedSeat bookedSeat) {
        bookedSeats.add(bookedSeat);
        bookedSeat.setBooking(this);
    }

    public void removeBookedSeat(BookedSeat bookedSeat) {
        bookedSeats.remove(bookedSeat);
        bookedSeat.setBooking(null);
    }

    public boolean isPending() {
        return status == BookingStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canBeCancelled() {
        return (isPending() || isConfirmed()) &&
                LocalDateTime.now().isBefore(show.getShowDateTime().minusHours(2));
    }

    public void confirmBooking() {
        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancelBooking(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void expireBooking() {
        this.status = BookingStatus.EXPIRED;
    }

    public boolean isPaymentCompleted() {
        return paymentDate != null && paymentReference != null;
    }

    public String getShowDetails() {
        return String.format("%s - %s at %s",
                show.getMovie().getTitle(),
                show.getShowDate(),
                show.getShowTime());
    }

    public String getTheaterDetails() {
        return String.format("%s - %s",
                show.getScreen().getTheater().getName(),
                show.getScreen().getName());
    }

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    // Booking Status enum
    public enum BookingStatus {
        PENDING("Booking is pending confirmation"),
        CONFIRMED("Booking is confirmed"),
        CANCELLED("Booking has been cancelled"),
        EXPIRED("Booking has expired"),
        COMPLETED("Booking is completed (show attended)"),
        NO_SHOW("Customer did not show up");

        private final String description;

        BookingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}