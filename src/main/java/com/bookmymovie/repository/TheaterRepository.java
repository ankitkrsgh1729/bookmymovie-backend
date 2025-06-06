package com.bookmymovie.repository;

import com.bookmymovie.constants.TheaterConstant;
import com.bookmymovie.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    // Basic queries
    Page<Theater> findByStatusAndDeletedFalse(TheaterConstant.TheaterStatus status, Pageable pageable);

    Page<Theater> findByCityIgnoreCaseAndStatusAndDeletedFalse(
            String city, TheaterConstant.TheaterStatus status, Pageable pageable);

    Page<Theater> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

    List<Theater> findByTheaterTypeAndStatusAndDeletedFalse(
            TheaterConstant.TheaterType theaterType, TheaterConstant.TheaterStatus status);

    // Geographic search queries - Find theaters within radius
    @Query(value = """
            SELECT t.*, 
                   (6371 * acos(cos(radians(:latitude)) * cos(radians(t.latitude)) * 
                   cos(radians(t.longitude) - radians(:longitude)) + 
                   sin(radians(:latitude)) * sin(radians(t.latitude)))) AS distance
            FROM theaters t 
            WHERE t.deleted = false 
            AND t.status = 'ACTIVE'
            AND (6371 * acos(cos(radians(:latitude)) * cos(radians(t.latitude)) * 
                 cos(radians(t.longitude) - radians(:longitude)) + 
                 sin(radians(:latitude)) * sin(radians(t.latitude)))) <= :radiusKm
            ORDER BY distance
            """, nativeQuery = true)
    List<Object[]> findTheatersWithinRadius(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Integer radiusKm);

    // Find theaters with specific facilities
    @Query("SELECT DISTINCT t FROM Theater t JOIN t.facilities f " +
            "WHERE f IN :facilities AND t.status = :status AND t.deleted = false " +
            "GROUP BY t HAVING COUNT(DISTINCT f) = :facilitiesCount")
    Page<Theater> findByFacilitiesAndStatus(
            @Param("facilities") List<TheaterConstant.Facility> facilities,
            @Param("facilitiesCount") long facilitiesCount,
            @Param("status") TheaterConstant.TheaterStatus status,
            Pageable pageable);

    // Business analytics queries
    @Query("SELECT COUNT(t) FROM Theater t WHERE t.city = :city AND t.status = 'ACTIVE' AND t.deleted = false")
    long countActiveTheatersByCity(@Param("city") String city);

    @Query("SELECT t.city, COUNT(t) FROM Theater t " +
            "WHERE t.status = 'ACTIVE' AND t.deleted = false " +
            "GROUP BY t.city ORDER BY COUNT(t) DESC")
    List<Object[]> getTheaterCountByCity();

    // Check for duplicates
    boolean existsByNameIgnoreCaseAndCityIgnoreCaseAndDeletedFalse(String name, String city);
}
