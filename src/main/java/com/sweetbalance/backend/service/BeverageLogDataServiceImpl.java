package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.daily.DailySugarDTO;
import com.sweetbalance.backend.dto.response.weekly.WeeklyConsumeInfoDTO;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.repository.BeverageLogRepository;
import com.sweetbalance.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BeverageLogDataServiceImpl implements BeverageLogDataService {

    private final UserRepository userRepository;
    private final BeverageLogRepository beverageLogRepository;

    @Override
    public List<BeverageLog> findTodayBeverageLogsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay().minusNanos(1);

        //findAllByUserUserIdAndCreatedAtBetween
        return beverageLogRepository.findByUser_UserIdAndCreatedAtBetweenAndStatusOrderByCreatedAtDesc(
                userId, startOfToday, endOfToday, Status.ACTIVE
        );
    }

    @Override
    public List<BeverageLog> findTotalBeverageLogsByUserId(Long userId, Pageable pageable, String sort) {
        List<BeverageLog> beverageLogs;

        if (sort.equals("old"))
            beverageLogs = beverageLogRepository.findTotalByUserUserIdAndStatusOrderByCreatedAtAsc(userId, pageable, Status.ACTIVE);
        else // new
            beverageLogs = beverageLogRepository.findTotalByUserUserIdAndStatusOrderByCreatedAtDesc(userId, pageable, Status.ACTIVE);

        return beverageLogs;
    }

    @Override
    public WeeklyConsumeInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveEndDate = endDate.isAfter(today) ? today : endDate;

        List<BeverageLog> logs = beverageLogRepository.findByUser_UserIdAndCreatedAtBetweenAndStatusOrderByCreatedAtDesc(
                userId,
                startDate.atStartOfDay(),
                effectiveEndDate.atTime(23, 59, 59),
                Status.ACTIVE
        );

        int intake = logs.size();
        double totalSugar = logs.stream()
                .mapToDouble(log -> Math.max(0, log.getBeverageSize().getSugar() + log.getAdditionalSugar()))
                .sum();
        double totalCalories = logs.stream()
                .mapToDouble(log -> log.getBeverageSize().getCalories())
                .sum();

        Map<LocalDate, Double> dailySugar = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getCreatedAt().toLocalDate(),
                        Collectors.summingDouble(log -> log.getBeverageSize().getSugar() + log.getAdditionalSugar())
                ));

        long effectiveDays = ChronoUnit.DAYS.between(startDate, effectiveEndDate) + 1;
        double averageSugar = totalSugar / effectiveDays;

        List<DailySugarDTO> dailySugarList = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(effectiveEndDate); date = date.plusDays(1)) {
            dailySugarList.add(new DailySugarDTO(date, Math.round(dailySugar.getOrDefault(date, 0.0) * 10.0) / 10.0));
        }

        return WeeklyConsumeInfoDTO.builder()
                .intake(intake)
                .totalSugar((int) Math.round(totalSugar))
                .averageSugar(Math.round(averageSugar * 10.0) / 10.0)
                .totalCalories((int) Math.round(totalCalories))
                .unreadAlarmCount(getNumberOfUnreadLogWithinAWeek(userId))
                .dailySugar(dailySugarList)
                .build();
    }

    @Override
    public int getNumberOfUnreadLogWithinAWeek(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return beverageLogRepository
                .findByUser_UserIdAndCreatedAtAfterAndReadByUserFalse(userId, sevenDaysAgo)
                .size();
    }
}
