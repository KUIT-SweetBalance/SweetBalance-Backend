package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndLoginTypeAndDeletedAtIsNull(String email, LoginType loginType);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndDeletedAtIsNull(String providerId);
}
