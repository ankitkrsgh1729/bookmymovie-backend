package com.bookmymovie.repository;

import com.bookmymovie.constants.TheaterConstant;
import com.bookmymovie.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Basic queries
    List<Seat> findByScreenScreenIdAndDeletedFalse(Long screenId);

    List<Seat> findByScreenScreenIdAndStatusAndDeletedFalse(
            Long screenId, TheaterConstant.SeatStatus status);

    List<Seat> findByScreenScreenIdAndCategoryAndDeletedFalse(
            Long screenId, TheaterConstant.SeatCategory category);

    // Row and seat number queries
    List<Seat> findByScreenScreenIdAndRowLabelIgnoreCaseAndDeletedFalse(
            Long screenId, String rowLabel);

    Optional<Seat> findByScreenScreenIdAndRowNumberAndSeatNumberAndDeletedFalse(
            Long screenId, Integer rowNumber, Integer seatNumber);

    // Availability queries
    @Query("SELECT s FROM Seat s WHERE s.screen.screenId = :screenId " +
            "AND s.status = 'AVAILABLE' AND s.deleted = false " +
            "ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findAvailableSeatsByScreen(@Param("screenId") Long screenId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.screen.screenId = :screenId " +
            "AND s.status = 'AVAILABLE' AND s.deleted = false")
    long countAvailableSeatsByScreen(@Param("screenId") Long screenId);

    // Category-wise queries
    @Query("SELECT s.category, COUNT(s) FROM Seat s " +
            "WHERE s.screen.screenId = :screenId AND s.deleted = false " +
            "GROUP BY s.category ORDER BY s.category")
    List<Object[]> getSeatCountByCategory(@Param("screenId") Long screenId);

    @Query("SELECT s.category, COUNT(s) FROM Seat s " +
            "WHERE s.screen.screenId = :screenId AND s.status = 'AVAILABLE' AND s.deleted = false " +
            "GROUP BY s.category ORDER BY s.category")
    List<Object[]> getAvailableSeatCountByCategory(@Param("screenId") Long screenId);

    // Special seat queries
    List<Seat> findByScreenScreenIdAndIsWheelchairAccessibleTrueAndDeletedFalse(Long screenId);

    List<Seat> findByScreenScreenIdAndIsCoupleSeATTrueAndDeletedFalse(Long screenId);

    List<Seat> findByScreenScreenIdAndIsPremiumTrueAndDeletedFalse(Long screenId);

    // Price range queries
    @Query("SELECT s FROM Seat s WHERE s.screen.screenId = :screenId " +
            "AND s.basePrice BETWEEN :minPrice AND :maxPrice " +
            "AND s.deleted = false ORDER BY s.basePrice")
    List<Seat> findSeatsByPriceRange(
            @Param("screenId") Long screenId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    // Seat map queries for booking
    @Query("SELECT s FROM Seat s WHERE s.screen.screenId = :screenId " +
            "AND s.deleted = false ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findSeatMapByScreen(@Param("screenId") Long screenId);

    // Bulk operations queries
    @Query("SELECT s FROM Seat s WHERE s.seatId IN :seatIds AND s.deleted = false")
    List<Seat> findBySeatIds(@Param("seatIds") List<Long> seatIds);

    // Validation queries
    boolean existsByScreenScreenIdAndRowNumberAndSeatNumberAndDeletedFalse(
            Long screenId, Integer rowNumber, Integer seatNumber);

    boolean existsByScreenScreenIdAndRowLabelIgnoreCaseAndSeatNumberAndDeletedFalse(
            Long screenId, String rowLabel, Integer seatNumber);

    /**
     * Find all seats for a specific screen ordered by row and seat number
     */
    List<Seat> findByScreenScreenIdOrderByRowLabelAscSeatNumberAsc(Long screenId);
}
