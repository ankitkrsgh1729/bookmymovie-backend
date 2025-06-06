package com.bookmymovie.repository;

import com.bookmymovie.constants.TheaterConstant;
import com.bookmymovie.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    // Basic queries
    List<Screen> findByTheaterTheaterIdAndStatusAndDeletedFalse(
            Long theaterId, TheaterConstant.ScreenStatus status);

    List<Screen> findByTheaterTheaterIdAndDeletedFalse(Long theaterId);

    Optional<Screen> findByTheaterTheaterIdAndNameIgnoreCaseAndDeletedFalse(
            Long theaterId, String name);

    // Screen type and feature queries
    List<Screen> findByScreenTypeAndStatusAndDeletedFalse(
            TheaterConstant.ScreenType screenType, TheaterConstant.ScreenStatus status);

    @Query("SELECT s FROM Screen s JOIN s.features f " +
            "WHERE f IN :features AND s.status = 'ACTIVE' AND s.deleted = false")
    List<Screen> findByFeaturesAndActive(@Param("features") List<TheaterConstant.ScreenFeature> features);

    // Capacity queries
    @Query("SELECT s FROM Screen s WHERE s.totalSeats >= :minSeats " +
            "AND s.status = 'ACTIVE' AND s.deleted = false ORDER BY s.totalSeats")
    List<Screen> findByMinimumCapacity(@Param("minSeats") Integer minSeats);

    // Business analytics
    @Query("SELECT s.screenType, COUNT(s) FROM Screen s " +
            "WHERE s.status = 'ACTIVE' AND s.deleted = false GROUP BY s.screenType")
    List<Object[]> getScreenCountByType();

    @Query("SELECT AVG(s.totalSeats) FROM Screen s " +
            "WHERE s.status = 'ACTIVE' AND s.deleted = false")
    Double getAverageSeatingCapacity();

    // Validation queries
    boolean existsByTheaterTheaterIdAndNameIgnoreCaseAndDeletedFalse(Long theaterId, String name);
}