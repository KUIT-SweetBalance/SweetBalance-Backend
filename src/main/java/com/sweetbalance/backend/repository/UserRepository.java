package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    // 사용자 이름으로 조회
    Optional<User> findByUsername(String username);

    // 특정 사용자 ID로 Beverage 목록 조회
    @Query("SELECT b FROM BeverageLog bl JOIN bl.beverage b WHERE bl.user.userId = :userId")
    List<Beverage> findBeveragesByUserId(@Param("userId") Long userId);
}
