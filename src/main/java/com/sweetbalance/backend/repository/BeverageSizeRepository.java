package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.dto.response.RecommendedBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeverageSizeRepository extends JpaRepository<BeverageSize, Long> {
  
  Optional<BeverageSize> findByBeverageAndVolume(Beverage beverage, int volume);

    @Query(value = "SELECT bs.* FROM beverage_sizes bs " +
            "JOIN beverages b ON bs.beverage_id = b.beverage_id " +
            "WHERE b.beverage_id != :excludeBeverageId " +
            "AND b.brand = :brand " +
            "ORDER BY ABS(bs.sugar - :targetSugar) ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<BeverageSize> findTopSimilarSizesByBrandAndSugar(
            @Param("excludeBeverageId") Long excludeBeverageId,
            @Param("brand") String brand,
            @Param("targetSugar") double targetSugar,
            @Param("limit") int limit);
}
