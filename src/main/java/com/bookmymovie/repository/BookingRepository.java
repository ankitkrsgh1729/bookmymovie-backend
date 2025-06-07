package com.bookmymovie.repository;

import com.bookmymovie.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserUserIdOrderByBookingDateDesc(Long userId);

    Page<Booking> findByUserUserIdOrderByBookingDateDesc(Long userId, Pageable pageable);

    List<Booking> findByShowShowIdAndStatusOrderByBookingDate(Long showId, Booking.BookingStatus status);

    List<Booking> findByStatusAndBookingDateBetween(
            Booking.BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // ==================== USER BOOKING QUERIES ====================

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate " +
            "ORDER BY b.bookingDate DESC")
    Page<Booking> findUserBookingsByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.status IN :statuses ORDER BY b.bookingDate DESC")
    List<Booking> findUserBookingsByStatus(
            @Param("userId") Long userId,
            @Param("statuses") List<Booking.BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.show.showDate >= :fromDate " +
            "ORDER BY b.show.showDate ASC, b.show.showTime ASC")
    List<Booking> findUpcomingUserBookings(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate);

    // ==================== SHOW BOOKING QUERIES ====================

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.showId = :showId " +
            "AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsForShow(@Param("showId") Long showId);

    @Query("SELECT SUM(b.numberOfSeats) FROM Booking b WHERE b.show.showId = :showId " +
            "AND b.status = 'CONFIRMED'")
    Optional<Integer> getTotalBookedSeatsForShow(@Param("showId") Long showId);

    @Query("SELECT b FROM Booking b WHERE b.show.showId = :showId " +
            "AND b.status = 'CONFIRMED' " +
            "ORDER BY b.bookingDate")
    List<Booking> findConfirmedBookingsForShow(@Param("showId") Long showId);

    // ==================== PAYMENT QUERIES ====================

    List<Booking> findByPaymentStatusAndExpiryTimeBefore(
            Booking.PaymentStatus paymentStatus, LocalDateTime cutoffTime);

    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :paymentStatus " +
            "AND b.paymentDate >= :startDate AND b.paymentDate <= :endDate " +
            "ORDER BY b.paymentDate DESC")
    List<Booking> findPaymentsByStatusAndDateRange(
            @Param("paymentStatus") Booking.PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(b.finalAmount) FROM Booking b WHERE b.paymentStatus = 'COMPLETED' " +
            "AND b.paymentDate >= :startDate AND b.paymentDate <= :endDate")
    Optional<BigDecimal> getTotalRevenueByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==================== EXPIRY AND CLEANUP QUERIES ====================

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
            "AND b.expiryTime < :currentTime")
    List<Booking> findExpiredPendingBookings(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE Booking b SET b.status = 'EXPIRED' WHERE b.status = 'PENDING' " +
            "AND b.expiryTime < :currentTime")
    int expireOldPendingBookings(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND b.show.showDateTime < :cutoffTime")
    List<Booking> findCompletedShowBookings(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ==================== ANALYTICS QUERIES ====================

    @Query("SELECT DATE(b.bookingDate), COUNT(b), SUM(b.finalAmount) FROM Booking b " +
            "WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate " +
            "AND b.status = 'CONFIRMED' " +
            "GROUP BY DATE(b.bookingDate) ORDER BY DATE(b.bookingDate)")
    List<Object[]> getDailyBookingStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.show.movie.title, COUNT(b), SUM(b.finalAmount) FROM Booking b " +
            "WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate " +
            "AND b.status = 'CONFIRMED' " +
            "GROUP BY b.show.movie.movieId, b.show.movie.title " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> getPopularMovieBookings(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.show.screen.theater.name, COUNT(b), SUM(b.finalAmount) FROM Booking b " +
            "WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate " +
            "AND b.status = 'CONFIRMED' " +
            "GROUP BY b.show.screen.theater.theaterId, b.show.screen.theater.name " +
            "ORDER BY SUM(b.finalAmount) DESC")
    List<Object[]> getTheaterBookingRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT EXTRACT(HOUR FROM b.booking_date) as hour, " +
            "COUNT(*) as booking_count, " +
            "AVG(b.final_amount) as avg_amount " +
            "FROM bookings b WHERE b.booking_date BETWEEN :startDate AND :endDate " +
            "AND b.status = 'CONFIRMED' " +
            "GROUP BY EXTRACT(HOUR FROM b.booking_date) " +
            "ORDER BY hour", nativeQuery = true)
    List<Object[]> getHourlyBookingPattern(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==================== REVENUE QUERIES ====================

    @Query("SELECT SUM(b.finalAmount) FROM Booking b WHERE b.paymentStatus = 'COMPLETED' " +
            "AND DATE(b.paymentDate) = :date")
    Optional<BigDecimal> getDailyRevenue(@Param("date") LocalDate date);

    @Query("SELECT b.paymentMethod, COUNT(b), SUM(b.finalAmount) FROM Booking b " +
            "WHERE b.paymentStatus = 'COMPLETED' " +
            "AND b.paymentDate >= :startDate AND b.paymentDate <= :endDate " +
            "GROUP BY b.paymentMethod ORDER BY COUNT(b) DESC")
    List<Object[]> getPaymentMethodStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(b.finalAmount) FROM Booking b WHERE b.paymentStatus = 'COMPLETED' " +
            "AND b.paymentDate >= :startDate AND b.paymentDate <= :endDate")
    Optional<BigDecimal> getAverageBookingValue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==================== CANCELLATION QUERIES ====================

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED' " +
            "AND b.cancellationDate >= :startDate AND b.cancellationDate <= :endDate")
    Long getCancellationCount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.status = 'CANCELLED' " +
            "AND b.refundAmount IS NULL " +
            "AND b.cancellationDate >= :cutoffDate")
    List<Booking> findPendingRefunds(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ==================== SEARCH QUERIES ====================

    @Query("SELECT b FROM Booking b WHERE " +
            "(:bookingReference IS NULL OR b.bookingReference LIKE %:bookingReference%) " +
            "AND (:contactEmail IS NULL OR b.contactEmail LIKE %:contactEmail%) " +
            "AND (:contactPhone IS NULL OR b.contactPhone LIKE %:contactPhone%) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus) " +
            "AND (:startDate IS NULL OR b.bookingDate >= :startDate) " +
            "AND (:endDate IS NULL OR b.bookingDate <= :endDate) " +
            "ORDER BY b.bookingDate DESC")
    Page<Booking> searchBookings(
            @Param("bookingReference") String bookingReference,
            @Param("contactEmail") String contactEmail,
            @Param("contactPhone") String contactPhone,
            @Param("status") Booking.BookingStatus status,
            @Param("paymentStatus") Booking.PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status " +
            "AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Long countBookingsByStatusAndDateRange(
            @Param("status") Booking.BookingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
            "COUNT(CASE WHEN b.status = 'CONFIRMED' THEN 1 END) as confirmed, " +
            "COUNT(CASE WHEN b.status = 'CANCELLED' THEN 1 END) as cancelled, " +
            "COUNT(CASE WHEN b.status = 'EXPIRED' THEN 1 END) as expired, " +
            "COUNT(*) as total " +
            "FROM Booking b WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Object[] getBookingStatusSummary(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(b.numberOfSeats) FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "AND b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Optional<Double> getAverageSeatsPerBooking(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ==================== SEAT OCCUPANCY QUERIES ====================

    @Query("SELECT bs.seat.seatId FROM BookingSeat bs " +
            "WHERE bs.booking.show.showId = :showId " +
            "AND bs.booking.status = 'CONFIRMED'")
    List<Long> getBookedSeatIdsForShow(@Param("showId") Long showId);

    @Query("SELECT bs.seat.rowLabel, bs.seat.seatNumber FROM BookingSeat bs " +
            "WHERE bs.booking.show.showId = :showId " +
            "AND bs.booking.status = 'CONFIRMED' " +
            "ORDER BY bs.seat.rowLabel, bs.seat.seatNumber")
    List<Object[]> getBookedSeatPositionsForShow(@Param("showId") Long showId);

    // ==================== USER ANALYTICS ====================

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.status = 'CONFIRMED'")
    Long getUserBookingCount(@Param("userId") Long userId);

    @Query("SELECT SUM(b.finalAmount) FROM Booking b WHERE b.user.userId = :userId " +
            "AND b.paymentStatus = 'COMPLETED'")
    Optional<BigDecimal> getUserTotalSpending(@Param("userId") Long userId);

    @Query("SELECT b.user.email, COUNT(b), SUM(b.finalAmount) FROM Booking b " +
            "WHERE b.bookingDate >= :startDate AND b.bookingDate <= :endDate " +
            "AND b.status = 'CONFIRMED' " +
            "GROUP BY b.user.userId, b.user.email " +
            "ORDER BY SUM(b.finalAmount) DESC")
    List<Object[]> getTopCustomers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}