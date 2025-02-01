package com.sweetbalance.backend.dto.response;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class WeeklyInfoDTO {
    private int intake;
    private double totalSugar;
    private double averageSugar;
    private double totalCalories;
    private List<DailySugarDTO> dailySugar;
}