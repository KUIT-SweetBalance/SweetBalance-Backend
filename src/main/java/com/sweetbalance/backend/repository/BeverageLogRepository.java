package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.enums.common.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BeverageLogRepository extends JpaRepository<BeverageLog,Long> {
    List<BeverageLog> findAllByUserUserIdAndStatusAndUpdatedAtBetween(Long userId, Status status, LocalDateTime start, LocalDateTime end);
    List<BeverageLog> findAllByUserUserIdAndStatusAndCreatedAtBetween(Long userId, Status status, LocalDateTime start, LocalDateTime end);
    List<BeverageLog> findByUser_UserIdAndCreatedAtBetweenAndStatusOrderByCreatedAtDesc(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime, Status status);
    List<BeverageLog> findTotalByUserUserIdAndStatusOrderByCreatedAtAsc(Long userId, Pageable pageable, Status status);
    List<BeverageLog> findTotalByUserUserIdAndStatusOrderByCreatedAtDesc(Long userId, Pageable pageable, Status status);
    List<BeverageLog> findByUser_UserIdAndCreatedAtAfterAndReadByUserFalse(Long userId, LocalDateTime dateTime);
}
