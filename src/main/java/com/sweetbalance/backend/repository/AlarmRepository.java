package com.sweetbalance.backend.repository;

import com.sweetbalance.backend.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm,Long> {

    //일치하는 userId와 해당하는 날짜 구간의 모든 알람을 찾는다.
    List<Alarm> findAllByLogUserUserIdAndUpdatedAtBetween(
            Long userId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
