package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.weekly.WeeklyConsumeInfoDTO;
import com.sweetbalance.backend.entity.BeverageLog;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BeverageLogDataService {

    List<BeverageLog> findTodayBeverageLogsByUserId(Long userId);
    List<BeverageLog> findTotalBeverageLogsByUserId(Long userId, Pageable pageable);

    WeeklyConsumeInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate);

    int getNumberOfUnreadLogWithinAWeek();
}
