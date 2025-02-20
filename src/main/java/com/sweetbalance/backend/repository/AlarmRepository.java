package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Alarm;
import com.sweetbalance.backend.enums.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm,Long> {

    //일치하는 userId와 해당하는 날짜 구간의 모든 알람을 찾는다.
    List<Alarm> findAllByLogUserUserIdAndUpdatedAtBetween(
            Long userId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    List<Alarm> findAllByLogUserUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    List<Alarm> findAllByLogUserUserIdAndLogCreatedAtBetween(
            Long userId,
            LocalDateTime oneWeekAgo,
            LocalDateTime now
    );

    Optional<Alarm> findByLogLogId(Long logId);

    List<Alarm> findAllByLogUserUserIdAndLogCreatedAtBetweenAndLogStatus(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay, Status status);
}
