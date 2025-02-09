package com.sweetbalance.backend.dto.response.daily;

import java.time.LocalDate;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class DailySugarDTO {
    private LocalDate date;
    private double sugar;
}