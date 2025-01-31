package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeverageSizeRepository extends JpaRepository<BeverageSize,Long> {

    Optional<BeverageSize> findByBeverageAndVolume(Beverage beverage, int volume);

}
