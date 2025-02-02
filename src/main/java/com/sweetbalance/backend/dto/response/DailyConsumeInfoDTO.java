package com.sweetbalance.backend.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class DailyConsumeInfoDTO {
    private int beverageCount;
    private int totalSugar;
}
