package com.bookmymovie.entity;

import com.bookmymovie.constants.TheaterConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theaters",
        indexes = {
                @Index(name = "idx_theater_city", columnList = "city"),
                @Index(name = "idx_theater_location", columnList = "latitude, longitude"),
                @Index(name = "idx_theater_status", columnList = "status")
        })
@SQLDelete(sql = "UPDATE theaters SET deleted = true WHERE theater_id = ?")
@Where(clause = "deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Theater extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theater_id")
    private Long theaterId;

    @NotBlank(message = "Theater name is required")
    @Size(min = 2, max = 100, message = "Theater name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City name cannot exceed 100 characters")
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State name cannot exceed 100 characters")
    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    @Column(name = "pincode", nullable = false, length = 6)
    private String pincode;

    // Geographic coordinates for location-based search
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email", length = 100)
    private String email;

    @NotNull(message = "Theater type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "theater_type", nullable = false)
    private TheaterConstant.TheaterType theaterType;

    @NotNull(message = "Theater status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TheaterConstant.TheaterStatus status = TheaterConstant.TheaterStatus.ACTIVE;

    // Operating hours
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    // Facilities and amenities
    @ElementCollection
    @CollectionTable(
            name = "theater_facilities",
            joinColumns = @JoinColumn(name = "theater_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "facility")
    @Builder.Default
    private List<TheaterConstant.Facility> facilities = new ArrayList<>();

    // One-to-Many relationship with Screens
    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Screen> screens = new ArrayList<>();

    // Business logic methods
    public void addScreen(Screen screen) {
        screens.add(screen);
        screen.setTheater(this);
    }

    public void removeScreen(Screen screen) {
        screens.remove(screen);
        screen.setTheater(null);
    }

    public int getTotalScreens() {
        return screens.size();
    }

    public int getTotalSeats() {
        return screens.stream()
                .mapToInt(Screen::getTotalSeats)
                .sum();
    }

    public boolean isOperational() {
        return status == TheaterConstant.TheaterStatus.ACTIVE;
    }

    public boolean isOpenAt(LocalTime time) {
        if (openingTime == null || closingTime == null) {
            return true; // 24/7 operation
        }
        return !time.isBefore(openingTime) && !time.isAfter(closingTime);
    }

    // Soft delete flag
    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;
}