package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.BeverageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BeverageLogRepository extends JpaRepository<BeverageLog,Long> {
    List<BeverageLog> findAllByUserUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<BeverageLog> findByUser_UserIdAndCreatedAtBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
    Page<BeverageLog> findAllByUserUserId(Long userId, Pageable pageable);
}
