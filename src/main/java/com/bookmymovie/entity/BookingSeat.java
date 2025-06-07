package com.bookmymovie.entity;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;


@Entity
@Table(name = "booking_seats",
        indexes = {
                @Index(name = "idx_booking_seat_booking", columnList = "booking_id"),
                @Index(name = "idx_booking_seat_seat", columnList = "seat_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"booking", "seat"})
@ToString(exclude = {"booking", "seat"})
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_seat_id")
    private Long bookingSeatId;

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

    @Column(name = "seat_category", length = 50)
    private String seatCategory;

    @Column(name = "seat_row", length = 5)
    private String seatRow;

    @Column(name = "seat_number")
    private Integer seatNumber;

    // For display purposes
    public String getSeatIdentifier() {
        return seatRow + seatNumber;
    }

    public String getSeatDescription() {
        return String.format("Seat %s%d (%s - ₹%.2f)",
                seatRow, seatNumber, seatCategory, seatPrice);
    }
}