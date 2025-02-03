package com.sweetbalance.backend.dto.response;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class WeeklyInfoDTO {
    private int intake;
    private int totalSugar;
    private double averageSugar;
    private int totalCalories;
    private List<DailySugarDTO> dailySugar;
}