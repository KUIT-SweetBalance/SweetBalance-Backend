package com.sweetbalance.backend.dto.response.daily;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class DailyConsumeInfoDTO {
    private int totalSugar;
    private int additionalSugar;
    private int beverageCount;
    private int unreadAlarmCount;
}
