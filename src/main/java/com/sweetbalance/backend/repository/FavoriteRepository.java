package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.Favorite;
import com.sweetbalance.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  
    List<Favorite> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Favorite> findByUserAndBeverage(User user, Beverage beverage);

}
