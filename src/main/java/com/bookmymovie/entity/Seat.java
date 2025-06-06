package com.bookmymovie.entity;
import com.bookmymovie.constants.TheaterConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seats",
        indexes = {
                @Index(name = "idx_seat_screen", columnList = "screen_id"),
                @Index(name = "idx_seat_row_number", columnList = "screen_id, row_number, seat_number"),
                @Index(name = "uk_seat_screen_row_number", columnList = "screen_id, row_number, seat_number", unique = true)
        })
@SQLDelete(sql = "UPDATE seats SET deleted = true WHERE seat_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"screen"})
@ToString(exclude = {"screen"})
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @NotNull(message = "Screen is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @NotBlank(message = "Row label is required")
    @Size(min = 1, max = 10, message = "Row label must be between 1 and 10 characters")
    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel; // A, B, C, etc.

    @Positive(message = "Row number must be positive")
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber; // 1, 2, 3, etc.

    @Positive(message = "Seat number must be positive")
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber; // 1, 2, 3, etc.

    @NotNull(message = "Seat category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TheaterConstant.SeatCategory category;

    @NotNull(message = "Seat status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TheaterConstant.SeatStatus status = TheaterConstant.SeatStatus.AVAILABLE;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be positive")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // Physical characteristics
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private TheaterConstant.SeatType seatType;

    // Special properties
    @Column(name = "is_wheelchair_accessible")
    @Builder.Default
    private Boolean isWheelchairAccessible = false;

    @Column(name = "is_couple_seat")
    @Builder.Default
    private Boolean isCoupleSeAT = false;

    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    // Additional features
    @ElementCollection
    @CollectionTable(
            name = "seat_features",
            joinColumns = @JoinColumn(name = "seat_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "feature")
    private List<TheaterConstant.SeatFeature> features = new ArrayList<>();

    // Business logic methods
    public String getSeatIdentifier() {
        return rowLabel + seatNumber;
    }

    public boolean isAvailable() {
        return status == TheaterConstant.SeatStatus.AVAILABLE;
    }

    public boolean isBlocked() {
        return status == TheaterConstant.SeatStatus.BLOCKED;
    }

    public boolean isUnderMaintenance() {
        return status == TheaterConstant.SeatStatus.UNDER_MAINTENANCE;
    }

    public boolean canBeBooked() {
        return isAvailable() && !isUnderMaintenance();
    }

    public BigDecimal getEffectivePrice() {
        BigDecimal effectivePrice = basePrice;

        // Apply premium charges
        if (isPremium) {
            effectivePrice = effectivePrice.multiply(new BigDecimal("1.2")); // 20% premium
        }

        // Apply couple seat charges
        if (isCoupleSeAT) {
            effectivePrice = effectivePrice.multiply(new BigDecimal("1.5")); // 50% extra
        }

        return effectivePrice;
    }

    public boolean hasFeature(TheaterConstant.SeatFeature feature) {
        return features.contains(feature);
    }

    public boolean isInRow(String rowLabel) {
        return this.rowLabel.equalsIgnoreCase(rowLabel);
    }

    public boolean isInCategory(TheaterConstant.SeatCategory category) {
        return this.category == category;
    }

    // Validation methods
    public void validateSeatConfiguration() {
        if (isCoupleSeAT && seatNumber % 2 != 1) {
            throw new IllegalStateException("Couple seats should start with odd seat numbers");
        }

        if (isWheelchairAccessible && rowNumber > 3) {
            throw new IllegalStateException("Wheelchair accessible seats should be in first 3 rows");
        }
    }

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;
}