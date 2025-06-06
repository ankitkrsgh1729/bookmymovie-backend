package com.bookmymovie.entity;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screens",
        indexes = {
                @Index(name = "idx_screen_theater", columnList = "theater_id"),
                @Index(name = "idx_screen_status", columnList = "status"),
                @Index(name = "uk_screen_theater_name", columnList = "theater_id, name", unique = true)
        })
@SQLDelete(sql = "UPDATE screens SET deleted = true WHERE screen_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"theater", "seats", "shows"})
@ToString(exclude = {"theater", "seats", "shows"})
public class Screen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screen_id")
    private Long screenId;

    @NotBlank(message = "Screen name is required")
    @Size(min = 1, max = 50, message = "Screen name must be between 1 and 50 characters")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull(message = "Theater is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @NotNull(message = "Screen type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false)
    private TheaterConstant.ScreenType screenType;

    @NotNull(message = "Sound system is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "sound_system", nullable = false)
    private TheaterConstant.SoundSystem soundSystem;

    @NotNull(message = "Screen status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TheaterConstant.ScreenStatus status = TheaterConstant.ScreenStatus.ACTIVE;

    // Screen dimensions
    @Positive(message = "Total rows must be positive")
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @Positive(message = "Total seats must be positive")
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    // Features and capabilities
    @ElementCollection
    @CollectionTable(
            name = "screen_features",
            joinColumns = @JoinColumn(name = "screen_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "feature")
    @Builder.Default
    private List<TheaterConstant.ScreenFeature> features = new ArrayList<>();

    // One-to-Many relationship with Seats
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    // One-to-Many relationship with Shows (will create later)
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Show> shows = new ArrayList<>();

    // Business logic methods
    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setScreen(this);
    }

    public void removeSeat(Seat seat) {
        seats.remove(seat);
        seat.setScreen(null);
    }

    public void addShow(Show show) {
        shows.add(show);
        show.setScreen(this);
    }

    public void removeShow(Show show) {
        shows.remove(show);
        show.setScreen(null);
    }

    public boolean isActive() {
        return status == TheaterConstant.ScreenStatus.ACTIVE;
    }

    public boolean canAccommodateShow() {
        return isActive() && !seats.isEmpty();
    }

    public long getAvailableSeats() {
        return seats.stream()
                .filter(seat -> seat.getStatus() == TheaterConstant.SeatStatus.AVAILABLE)
                .count();
    }

    public List<Seat> getSeatsByCategory(TheaterConstant.SeatCategory category) {
        return seats.stream()
                .filter(seat -> seat.getCategory() == category)
                .toList();
    }

    public boolean hasFeature(TheaterConstant.ScreenFeature feature) {
        return features.contains(feature);
    }

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;
}