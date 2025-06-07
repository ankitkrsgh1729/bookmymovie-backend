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
                @Index(name = "idx_booking_reference", columnList = "booking_reference"),
                @Index(name = "idx_booking_payment", columnList = "payment_status")
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

    @NotNull(message = "Booking reference is required")
    @Column(name = "booking_reference", unique = true, nullable = false, length = 20)
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

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    // Seat Information
    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat must be booked")
    @Max(value = 10, message = "Maximum 10 seats can be booked at once")
    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingSeat> bookedSeats;

    // Pricing Information
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "convenience_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal convenienceFee = BigDecimal.ZERO;

    @Column(name = "taxes", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxes = BigDecimal.ZERO;

    @NotNull(message = "Final amount is required")
    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    // Booking Status
    @NotNull(message = "Booking status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // Payment Information
    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 50)
    private String paymentReference;

    @Column(name = "payment_gateway_response", length = 1000)
    private String paymentGatewayResponse;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Contact Information
    @NotBlank(message = "Contact email is required")
    @Email(message = "Valid email is required")
    @Column(name = "contact_email", nullable = false, length = 100)
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    // Additional Information
    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "booking_source")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingSource source = BookingSource.WEB;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    // Business Logic Methods

    @PrePersist
    private void generateBookingReference() {
        if (bookingReference == null) {
            bookingReference = generateReference();
        }
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
        }
        if (expiryTime == null) {
            expiryTime = bookingDate.plusMinutes(15); // 15 minutes to complete payment
        }
    }

    private String generateReference() {
        return "BK" + System.currentTimeMillis() +
                String.format("%02d", (int)(Math.random() * 100));
    }

    public void calculateFinalAmount() {
        BigDecimal baseAmount = totalAmount.subtract(discountAmount);
        BigDecimal amountWithFees = baseAmount.add(convenienceFee);
        this.finalAmount = amountWithFees.add(taxes);
    }

    public boolean isExpired() {
        return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean canBeCancelled() {
        return (status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING) &&
                show.getShowDateTime().isAfter(LocalDateTime.now().plusHours(2));
    }

    public boolean canBeModified() {
        return status == BookingStatus.PENDING && !isExpired();
    }

    public void confirmBooking() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        if (paymentStatus != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be completed before confirmation");
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancelBooking(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Booking cannot be cancelled at this time");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancellationDate = LocalDateTime.now();
    }

    public void expireBooking() {
        if (status == BookingStatus.PENDING) {
            this.status = BookingStatus.EXPIRED;
        }
    }

    public void processRefund(BigDecimal amount) {
        if (status != BookingStatus.CANCELLED) {
            throw new IllegalStateException("Only cancelled bookings can be refunded");
        }
        this.refundAmount = amount;
        this.refundDate = LocalDateTime.now();
    }

    public void updatePaymentStatus(PaymentStatus newStatus, String reference, String gatewayResponse) {
        this.paymentStatus = newStatus;
        this.paymentReference = reference;
        this.paymentGatewayResponse = gatewayResponse;

        if (newStatus == PaymentStatus.COMPLETED) {
            this.paymentDate = LocalDateTime.now();
        }
    }

    public String getFormattedBookingReference() {
        return bookingReference;
    }

    public String getShowDetails() {
        return String.format("%s at %s on %s %s",
                show.getMovie().getTitle(),
                show.getScreen().getTheater().getName(),
                show.getShowDate(),
                show.getShowTime());
    }

    public boolean isPaymentPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public boolean isPaymentCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public boolean isPaymentFailed() {
        return paymentStatus == PaymentStatus.FAILED;
    }

    public long getTimeToExpiry() {
        if (expiryTime == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), expiryTime).toMinutes();
    }

    // Enums

    public enum BookingStatus {
        PENDING("Booking is pending payment"),
        CONFIRMED("Booking is confirmed"),
        CANCELLED("Booking has been cancelled"),
        EXPIRED("Booking has expired"),
        COMPLETED("Show has been completed"),
        NO_SHOW("Customer did not show up");

        private final String description;

        BookingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum PaymentStatus {
        PENDING("Payment is pending"),
        PROCESSING("Payment is being processed"),
        COMPLETED("Payment completed successfully"),
        FAILED("Payment failed"),
        REFUNDED("Payment has been refunded"),
        PARTIAL_REFUND("Partial refund processed");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        UPI("UPI Payment"),
        NET_BANKING("Net Banking"),
        WALLET("Digital Wallet"),
        CASH("Cash Payment"),
        GIFT_CARD("Gift Card");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BookingSource {
        WEB("Web Application"),
        MOBILE_APP("Mobile Application"),
        COUNTER("Theater Counter"),
        PHONE("Phone Booking"),
        PARTNER("Partner Platform");

        private final String description;

        BookingSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}