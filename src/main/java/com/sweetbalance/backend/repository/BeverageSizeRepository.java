package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.BeverageSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeverageSizeRepository extends JpaRepository<BeverageSize, Long> {

}