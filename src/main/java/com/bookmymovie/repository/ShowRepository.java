package com.bookmymovie.repository;

import com.bookmymovie.entity.Show;
import com.bookmymovie.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    // ==================== BASIC QUERIES ====================

    List<Show> findByMovieMovieIdAndShowDateOrderByShowTime(Long movieId, LocalDate showDate);

    List<Show> findByScreenScreenIdAndShowDateOrderByShowTime(Long screenId, LocalDate showDate);

    Page<Show> findByMovieMovieIdAndShowDateBetweenOrderByShowDateAscShowTimeAsc(
            Long movieId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Show> findByScreenTheaterTheaterIdAndShowDateOrderByShowTime(Long theaterId, LocalDate showDate);

    // ==================== CONFLICT DETECTION ====================

    @Query("SELECT s FROM Show s WHERE s.screen.screenId = :screenId " +
            "AND s.showDate = :showDate " +
            "AND s.status IN ('SCHEDULED', 'ONGOING') " +
            "AND ((s.showDateTime <= :endTime AND s.endTime >= :startTime))")
    List<Show> findConflictingShows(
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Show s WHERE s.screen.screenId = :screenId " +
            "AND s.showDate = :showDate " +
            "AND s.status = 'SCHEDULED' " +
            "AND s.showId != :excludeShowId " +
            "AND ((s.showDateTime <= :endTime AND s.endTime >= :startTime))")
    List<Show> findConflictingShowsExcluding(
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeShowId") Long excludeShowId);

    // ==================== SEARCH & FILTERING ====================

    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId " +
            "AND s.screen.theater.city = :city " +
            "AND s.showDate = :showDate " +
            "AND s.status = 'SCHEDULED' " +
            "ORDER BY s.showTime")
    List<Show> findShowsByMovieAndCityAndDate(
            @Param("movieId") Long movieId,
            @Param("city") String city,
            @Param("showDate") LocalDate showDate);

    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId " +
            "AND s.screen.theater.city = :city " +
            "AND s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'SCHEDULED' " +
            "ORDER BY s.showDate, s.showTime")
    List<Show> findShowsByMovieAndCityBetweenDates(
            @Param("movieId") Long movieId,
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Show s WHERE s.screen.theater.theaterId = :theaterId " +
            "AND s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status IN :statuses " +
            "ORDER BY s.showDate, s.showTime")
    List<Show> findShowsByTheaterAndDateRangeAndStatus(
            @Param("theaterId") Long theaterId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<Show.ShowStatus> statuses);

    // Geographic search
    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId " +
            "AND s.showDate = :showDate " +
            "AND s.status = 'SCHEDULED' " +
            "AND s.screen.theater.latitude BETWEEN :minLat AND :maxLat " +
            "AND s.screen.theater.longitude BETWEEN :minLng AND :maxLng " +
            "ORDER BY s.showTime")
    List<Show> findShowsByMovieAndDateNearLocation(
            @Param("movieId") Long movieId,
            @Param("showDate") LocalDate showDate,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng);

    // ==================== AVAILABILITY QUERIES ====================

    @Query("SELECT s FROM Show s WHERE s.status = 'SCHEDULED' " +
            "AND s.showDateTime > :currentDateTime " +
            "AND s.availableSeats > 0 " +
            "ORDER BY s.showDateTime")
    List<Show> findAvailableShows(@Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId " +
            "AND s.status = 'SCHEDULED' " +
            "AND s.showDateTime > :currentDateTime " +
            "AND s.availableSeats >= :requiredSeats " +
            "ORDER BY s.showDateTime")
    List<Show> findAvailableShowsForMovie(
            @Param("movieId") Long movieId,
            @Param("currentDateTime") LocalDateTime currentDateTime,
            @Param("requiredSeats") Integer requiredSeats);

    // ==================== TIME-BASED QUERIES ====================

    @Query("SELECT s FROM Show s WHERE s.showDate = :date " +
            "AND s.showTime BETWEEN :startTime AND :endTime " +
            "AND s.status = 'SCHEDULED' " +
            "ORDER BY s.showTime")
    List<Show> findShowsByTimeSlot(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT s FROM Show s WHERE s.status = 'SCHEDULED' " +
            "AND s.showDateTime BETWEEN :startDateTime AND :endDateTime " +
            "ORDER BY s.showDateTime")
    List<Show> findUpcomingShows(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT s FROM Show s WHERE s.status = 'SCHEDULED' " +
            "AND s.showDateTime <= :cutoffTime " +
            "ORDER BY s.showDateTime")
    List<Show> findShowsStartingSoon(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ==================== ANALYTICS QUERIES ====================

    @Query("SELECT s.movie.title, COUNT(s), AVG(s.bookedSeats * 100.0 / s.totalSeats) " +
            "FROM Show s WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' " +
            "GROUP BY s.movie.movieId, s.movie.title " +
            "ORDER BY COUNT(s) DESC")
    List<Object[]> getPopularMoviesByShowCount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT s.screen.theater.name, COUNT(s), SUM(s.bookedSeats * s.basePrice) " +
            "FROM Show s WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' " +
            "GROUP BY s.screen.theater.theaterId, s.screen.theater.name " +
            "ORDER BY SUM(s.bookedSeats * s.basePrice) DESC")
    List<Object[]> getTheaterRevenueAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT EXTRACT(HOUR FROM s.show_time) as hour, " +
            "COUNT(*) as show_count, " +
            "AVG(s.booked_seats * 100.0 / s.total_seats) as avg_occupancy " +
            "FROM shows s WHERE s.show_date BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' " +
            "GROUP BY EXTRACT(HOUR FROM s.show_time) " +
            "ORDER BY hour", nativeQuery = true)
    List<Object[]> getHourlyOccupancyAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT CASE WHEN EXTRACT(DOW FROM s.show_date) IN (0,6) " +
            "THEN 'WEEKEND' ELSE 'WEEKDAY' END as day_type, " +
            "COUNT(*) as show_count, " +
            "AVG(s.booked_seats * 100.0 / s.total_seats) as avg_occupancy " +
            "FROM shows s WHERE s.show_date BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' " +
            "GROUP BY day_type", nativeQuery = true)
    List<Object[]> getWeekdayVsWeekendAnalysis(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ==================== REVENUE QUERIES ====================

    @Query("SELECT SUM(s.bookedSeats * s.basePrice) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED'")
    Optional<Double> getTotalRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT s.showDate, SUM(s.bookedSeats * s.basePrice) " +
            "FROM Show s WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' " +
            "GROUP BY s.showDate " +
            "ORDER BY s.showDate")
    List<Object[]> getDailyRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ==================== SPECIAL QUERIES ====================

    @Query("SELECT s FROM Show s WHERE s.isPremiere = true " +
            "AND s.status = 'SCHEDULED' " +
            "AND s.showDateTime > :currentDateTime " +
            "ORDER BY s.showDateTime")
    List<Show> findUpcomingPremieres(@Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("SELECT s FROM Show s WHERE s.isSpecialScreening = true " +
            "AND s.status = 'SCHEDULED' " +
            "AND s.showDateTime > :currentDateTime " +
            "ORDER BY s.showDateTime")
    List<Show> findSpecialScreenings(@Param("currentDateTime") LocalDateTime currentDateTime);

    @Query("SELECT s FROM Show s WHERE s.availableSeats = 0 " +
            "AND s.status = 'SCHEDULED' " +
            "AND s.showDateTime > :currentDateTime")
    List<Show> findHousefullShows(@Param("currentDateTime") LocalDateTime currentDateTime);

    // ==================== MAINTENANCE QUERIES ====================

    @Query("SELECT s FROM Show s WHERE s.status = 'SCHEDULED' " +
            "AND s.showDateTime < :cutoffDateTime")
    List<Show> findExpiredScheduledShows(@Param("cutoffDateTime") LocalDateTime cutoffDateTime);

    @Query("SELECT s FROM Show s WHERE s.status = 'ONGOING' " +
            "AND s.endTime < :cutoffDateTime")
    List<Show> findOngoingShowsToComplete(@Param("cutoffDateTime") LocalDateTime cutoffDateTime);

    // ==================== STATISTICS ====================

    @Query("SELECT COUNT(s) FROM Show s WHERE s.showDate = :date AND s.status = 'SCHEDULED'")
    Long countScheduledShowsByDate(@Param("date") LocalDate date);

    @Query("SELECT AVG(s.bookedSeats * 100.0 / s.totalSeats) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED'")
    Optional<Double> getAverageOccupancyRate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT s.screen.theater.city, COUNT(s) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.screen.theater.city " +
            "ORDER BY COUNT(s) DESC")
    List<Object[]> getShowCountByCity(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}