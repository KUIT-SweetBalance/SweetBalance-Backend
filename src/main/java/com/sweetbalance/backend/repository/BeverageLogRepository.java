package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.BeverageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeverageLogRepository extends JpaRepository<BeverageLog,Long> {

}
