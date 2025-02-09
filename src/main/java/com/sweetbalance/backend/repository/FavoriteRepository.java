package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.Favorite;
import com.sweetbalance.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  
    List<Favorite> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Favorite> findByUserAndBeverage(User user, Beverage beverage);

    boolean existsByUser_UserIdAndBeverage_BeverageId(Long userId, Long beverageId);

    @Query("SELECT f.beverage.beverageId FROM Favorite f " +
            "WHERE f.user.userId = :userId AND f.beverage.beverageId IN :beverageIds")
    Set<Long> findFavoriteBeverageIdsByUser(
            @Param("userId") Long userId,
            @Param("beverageIds") List<Long> beverageIds
    );

}
