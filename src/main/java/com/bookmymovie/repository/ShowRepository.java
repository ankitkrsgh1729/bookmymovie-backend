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
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    // Basic queries
    List<Show> findByScreenScreenIdAndShowDateAndDeletedFalse(Long screenId, LocalDate showDate);

    List<Show> findByMovieMovieIdAndShowDateAndDeletedFalse(Long movieId, LocalDate showDate);

    Page<Show> findByShowDateBetweenAndStatusAndDeletedFalse(
            LocalDate startDate, LocalDate endDate, Show.ShowStatus status, Pageable pageable);

    // Screen availability for scheduling
    @Query("SELECT s FROM Show s WHERE s.screen.screenId = :screenId " +
            "AND s.showDate = :showDate AND s.status IN ('SCHEDULED', 'RUNNING') " +
            "AND s.deleted = false ORDER BY s.showTime")
    List<Show> findScheduledShowsByScreenAndDate(
            @Param("screenId") Long screenId, @Param("showDate") LocalDate showDate);

    // Check for overlapping shows
    @Query("SELECT COUNT(s) > 0 FROM Show s WHERE s.screen.screenId = :screenId " +
            "AND s.showDate = :showDate AND s.status IN ('SCHEDULED', 'RUNNING') " +
            "AND s.deleted = false " +
            "AND ((s.showTime <= :endTime AND s.endTime >= :startTime))")
    boolean existsOverlappingShow(
            @Param("screenId") Long screenId,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    // Movie and theater combinations
    @Query("SELECT DISTINCT s.screen.theater FROM Show s " +
            "WHERE s.movie.movieId = :movieId AND s.showDate = :showDate " +
            "AND s.status = 'SCHEDULED' AND s.deleted = false")
    List<Theater> findTheatersByMovieAndDate(
            @Param("movieId") Long movieId, @Param("showDate") LocalDate showDate);

    @Query("SELECT s FROM Show s WHERE s.movie.movieId = :movieId " +
            "AND s.screen.theater.city = :city AND s.showDate = :showDate " +
            "AND s.status = 'SCHEDULED' AND s.deleted = false " +
            "ORDER BY s.showTime")
    List<Show> findShowsByMovieCityAndDate(
            @Param("movieId") Long movieId,
            @Param("city") String city,
            @Param("showDate") LocalDate showDate);

    // Booking availability
    @Query("SELECT s FROM Show s WHERE s.availableSeats > 0 " +
            "AND s.status = 'SCHEDULED' AND s.deleted = false " +
            "AND s.showDate >= :fromDate ORDER BY s.showDate, s.showTime")
    List<Show> findAvailableShows(@Param("fromDate") LocalDate fromDate);

    // Business analytics
    @Query("SELECT s.showDate, COUNT(s) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false GROUP BY s.showDate ORDER BY s.showDate")
    List<Object[]> getShowCountByDateRange(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s.movie.title, COUNT(s) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false GROUP BY s.movie.movieId, s.movie.title " +
            "ORDER BY COUNT(s) DESC")
    List<Object[]> getMostScheduledMovies(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(s.bookedSeats * 100.0 / s.screen.totalSeats) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND s.deleted = false")
    Double getAverageOccupancyPercentage(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Pricing analytics
    @Query("SELECT s.movie.title, AVG(s.basePrice) FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false GROUP BY s.movie.movieId, s.movie.title")
    List<Object[]> getAveragePriceByMovie(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Show timing analysis
    @Query("SELECT s FROM Show s WHERE s.showTime BETWEEN :startTime AND :endTime " +
            "AND s.showDate = :showDate AND s.deleted = false ORDER BY s.showTime")
    List<Show> findShowsByTimeRange(
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    // Peak hours analysis (using native SQL for time extraction)
    @Query(value = "SELECT EXTRACT(HOUR FROM s.show_time) as hour, COUNT(s.*) as show_count " +
            "FROM shows s " +
            "WHERE s.show_date BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false " +
            "GROUP BY hour " +
            "ORDER BY hour", nativeQuery = true)
    List<Object[]> getShowCountByHour(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Weekend vs weekday analysis (using native SQL for date functions)
    @Query(value = "SELECT " +
            "CASE WHEN EXTRACT(DOW FROM s.show_date) IN (0,6) THEN 'WEEKEND' ELSE 'WEEKDAY' END as day_type, " +
            "COUNT(s.*) as show_count, " +
            "AVG(s.booked_seats * 100.0 / sc.total_seats) as avg_occupancy " +
            "FROM shows s " +
            "JOIN screens sc ON s.screen_id = sc.screen_id " +
            "WHERE s.show_date BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false " +
            "GROUP BY day_type", nativeQuery = true)
    List<Object[]> getWeekendVsWeekdayAnalysis(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Upcoming shows for a theater
    @Query("SELECT s FROM Show s WHERE s.screen.theater.theaterId = :theaterId " +
            "AND s.showDate >= :fromDate AND s.status = 'SCHEDULED' " +
            "AND s.deleted = false ORDER BY s.showDate, s.showTime")
    List<Show> findUpcomingShowsByTheater(
            @Param("theaterId") Long theaterId, @Param("fromDate") LocalDate fromDate);

    // Current running shows
    @Query("SELECT s FROM Show s WHERE s.status = 'RUNNING' " +
            "AND s.deleted = false ORDER BY s.showTime")
    List<Show> findCurrentlyRunningShows();

    // Shows nearing start time (for notifications)
    @Query("SELECT s FROM Show s WHERE s.status = 'SCHEDULED' " +
            "AND s.showDate = :today " +
            "AND s.showTime BETWEEN :currentTime AND :notificationWindow " +
            "AND s.deleted = false")
    List<Show> findShowsNearingStart(
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime,
            @Param("notificationWindow") LocalTime notificationWindow);

    // Housefull shows
    @Query("SELECT s FROM Show s WHERE s.availableSeats = 0 " +
            "AND s.status = 'SCHEDULED' AND s.deleted = false " +
            "ORDER BY s.showDate, s.showTime")
    List<Show> findHousefullShows();

    // Show performance metrics
    @Query("SELECT s.screen.theater.city, s.movie.title, " +
            "COUNT(s) as totalShows, " +
            "AVG(s.bookedSeats) as avgBookedSeats, " +
            "AVG(s.bookedSeats * 100.0 / s.screen.totalSeats) as avgOccupancy " +
            "FROM Show s WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.status = 'COMPLETED' AND s.deleted = false " +
            "GROUP BY s.screen.theater.city, s.movie.movieId, s.movie.title " +
            "ORDER BY avgOccupancy DESC")
    List<Object[]> getShowPerformanceMetrics(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Revenue potential analysis
    @Query("SELECT s FROM Show s " +
            "WHERE s.showDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false " +
            "ORDER BY (s.basePrice * s.screen.totalSeats) DESC")
    List<Show> findHighRevenueShows(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}