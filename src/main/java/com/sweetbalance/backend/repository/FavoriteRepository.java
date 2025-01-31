package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Favorite;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}