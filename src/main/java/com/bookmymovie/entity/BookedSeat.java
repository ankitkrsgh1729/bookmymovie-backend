package com.bookmymovie.entity;
import com.bookmymovie.constants.TheaterConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "booked_seats",
        indexes = {
                @Index(name = "idx_booked_seat_booking", columnList = "booking_id"),
                @Index(name = "idx_booked_seat_seat", columnList = "seat_id"),
                @Index(name = "uk_booked_seat_booking_seat", columnList = "booking_id, seat_id", unique = true)
        })
@SQLDelete(sql = "UPDATE booked_seats SET deleted = true WHERE booked_seat_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"booking", "seat"})
@ToString(exclude = {"booking", "seat"})
public class BookedSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booked_seat_id")
    private Long bookedSeatId;

    @NotNull(message = "Booking is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull(message = "Seat is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @NotNull(message = "Seat price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Seat price must be positive")
    @Column(name = "seat_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal seatPrice;

    // Seat information snapshot (for historical data)
    @NotBlank(message = "Seat identifier is required")
    @Column(name = "seat_identifier", nullable = false, length = 10)
    private String seatIdentifier;

    @NotBlank(message = "Row label is required")
    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @NotNull(message = "Seat category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_category", nullable = false)
    private TheaterConstant.SeatCategory seatCategory;

    // Business logic methods
    public String getDisplayName() {
        return seatIdentifier;
    }

    public boolean isSpecialSeat() {
        return seat.getIsCoupleSeAT() || seat.getIsPremium() || seat.getIsWheelchairAccessible();
    }

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;
}