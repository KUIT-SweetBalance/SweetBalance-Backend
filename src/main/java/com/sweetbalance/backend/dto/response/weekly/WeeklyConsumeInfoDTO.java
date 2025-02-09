package com.sweetbalance.backend.dto.response.weekly;

import com.sweetbalance.backend.dto.response.daily.DailySugarDTO;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class WeeklyConsumeInfoDTO {
    private int intake;
    private int totalSugar;
    private double averageSugar;
    private int totalCalories;
    private int unreadAlarmCount;
    private List<DailySugarDTO> dailySugar;
}