package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Beverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeverageRepository extends JpaRepository<Beverage, Long> {

    Optional<Beverage> findByName(String username);
}
