package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Beverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeverageRepository extends JpaRepository<Beverage, Long> {

    @Query("SELECT DISTINCT b.brand FROM Beverage b")
    List<String> findDistinctBrands();

    @Query(value = "SELECT * FROM beverages WHERE brand = :brandName ORDER BY consume_count DESC LIMIT :limit", nativeQuery = true)
    List<Beverage> findTopBeveragesByBrandOrderByConsumeCountDesc(@Param("brandName") String brandName, @Param("limit") int limit);
}