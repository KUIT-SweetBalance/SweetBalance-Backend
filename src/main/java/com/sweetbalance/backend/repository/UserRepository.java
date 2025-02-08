package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndLoginTypeAndDeletedAtIsNull(String email, LoginType loginType);

    Optional<User> findByProviderIdAndDeletedAtIsNull(String providerId);

    // 사용자 이름으로 조회
    Optional<User> findByUsername(String username);

    // email로 사용자 조회 (임시 비밀번호 발급용)
    Optional<User> findByEmail(String email);

    // 특정 사용자 ID로 Beverage 목록 조회
    @Query("SELECT b FROM BeverageLog bl JOIN bl.beverageSize b WHERE bl.user.userId = :userId")
    List<Beverage> findBeveragesByUserId(@Param("userId") Long userId);
}
